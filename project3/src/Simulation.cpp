#include "Simulation.h"
#include "Individual.h"
#include <iostream>
#include <mpi.h>
#include <stdio.h>
#include <string>

inline float dist(Individual a, Individual b)
{
    return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
}

struct Date
{
    int month;
    int day;
    int hours;
    int minutes;
    int seconds;
};

Date parseDate(int time)
{
    int seconds = time % 60;
    time = (time - seconds) / 60;

    int minutes = time % 60;
    time = (time - minutes) / 60;

    int hours = time % 24;
    time = (time - hours) / 24;

    int day = time % 30;
    int month = (time - day) / 30;

    return Date{month, day, hours, minutes, seconds};
}

void logStatistics(std::vector<Individual> &local_individuals, SimulationParameters &p, int rank, int group_size, int time)
{
    for (Country &country : p.COUNTRIES)
    {
        int local_susceptible = 0;
        int local_infected = 0;
        int local_recovered = 0;
        for (Individual &ind : local_individuals)
        {
            if (ind.parentNodeRank(p, group_size) != rank || !ind.isIn(country))
            {
                continue;
            }

            switch (ind.Health())
            {
            case Individual::Susceptible:
                local_susceptible++;
                break;

            case Individual::Infected:
                local_infected++;
                break;

            case Individual::Recovered:
                local_recovered++;
                break;
            }
        }

        int total_susceptible;
        int total_infected;
        int total_recovered;
        MPI_Reduce(&local_susceptible, &total_susceptible, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
        MPI_Reduce(&local_infected, &total_infected, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
        MPI_Reduce(&local_recovered, &total_recovered, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
        if (rank == 0)
        {
            Date date = parseDate(time);
            printf("Country: %s\n", country.name.c_str());
            printf("Month: %02d, Day: %02d, Time %02d:%02d:%02d --- sus %d, inf %d, rec %d\n", date.month, date.day, date.hours, date.minutes, date.seconds, total_susceptible, total_infected, total_recovered);
        }
    }
    if (rank == 0)
        printf("\n");
}

void saveSnapshot(std::vector<Individual> &local_individuals, int rank, int group_size, int time, SimulationParameters &p)
{
    std::string timeStep = std::to_string(time * p.TIME_STEP);
    timeStep = std::string(9 - std::min(9, int(timeStep.length())), '0') + timeStep;

    std::string filename = "snapshots/snapshot_" + timeStep + ".csv";
    if (rank == 0)
    {
        FILE *file = fopen(filename.c_str(), "w");
        fprintf(file, "x,y,health\n");
        fclose(file);
    }

    for (int i = 0; i < group_size; i++)
    {
        if (rank == i)
        {
            FILE *file = fopen(filename.c_str(), "a");
            for (Individual &ind : local_individuals)
            {
                if (ind.parentNodeRank(p, group_size) != rank)
                {
                    continue;
                }
                fprintf(file, "%f,%f,%d\n", ind.x, ind.y, ind.Health());
            }
            fclose(file);
        }
        MPI_Barrier(MPI_COMM_WORLD);
    }
}

void initializeIndividualsPool(std::vector<Individual> &local_individuals, std::default_random_engine r_engine, int local_N, int local_I, SimulationParameters &params)
{
    local_individuals.reserve(local_N);
    for (int i = 0; i < local_I; i++)
    {
        local_individuals.emplace_back(r_engine, Individual::Infected, params);
    }
    for (int i = local_I; i < local_N; i++)
    {
        local_individuals.emplace_back(r_engine, Individual::Susceptible, params);
    }
}

MPI_Datatype MPI_Individual;

int main(int argc, char *argv[])
{
    MPI_Init(&argc, &argv);
    int rank, group_size;
    MPI_Comm_size(MPI_COMM_WORLD, &group_size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    std::default_random_engine r_engine(rank);

    // Setup simulation parameters
    SimulationParameters params{};
    params.SPEED = 0.1;
    params.WORLD_WIDTH = 10000.;
    params.WORLD_HEIGHT = 10000.;
    params.HORIZONTAL_BLOCKS = 2;
    params.VERTICAL_BLOCKS = 2;
    params.BLOCK_WIDTH = params.WORLD_WIDTH / float(params.HORIZONTAL_BLOCKS);
    params.BLOCK_HEIGHT = params.WORLD_HEIGHT / float(params.VERTICAL_BLOCKS);
    params.TIME_STEP = 200;
    params.SIMULATION_STEPS = 10000;
    params.SPREADING_DISTANCE = 500.;
    params.SPREADING_DISTANCE2 = params.SPREADING_DISTANCE * params.SPREADING_DISTANCE;
    params.INITIAL_INDIVIDUALS = 500;
    params.INITIAL_INFECTED = 50;
    params.COUNTRIES = {
        Country{0, params.WORLD_WIDTH, 0, params.WORLD_HEIGHT, "World"},
        Country{1000, 3000, 1000, 3000, "Italy"},
        Country{6000, 9000, 6000, 9000, "Germany"},
    };

    params.SAVE_SNAPSHOTS = true;

    // Create MPI type for individual
    int blockCount = 5;
    std::vector<int> blockLengths{1, 1, 1, 1, 1};
    std::vector<long> blockDisplacements{0, 4, 8, 12, 16};
    std::vector<MPI_Datatype> blockTypes{MPI_FLOAT, MPI_FLOAT, MPI_FLOAT, MPI_UNSIGNED, MPI_INT};
    MPI_Type_create_struct(blockCount, blockLengths.data(), blockDisplacements.data(), blockTypes.data(), &MPI_Individual);
    MPI_Type_commit(&MPI_Individual);

    // Initialize local pool of individuals
    int local_N = params.INITIAL_INDIVIDUALS / group_size;
    int local_I = params.INITIAL_INFECTED / group_size;
    std::vector<Individual> local_individuals;
    initializeIndividualsPool(local_individuals, r_engine, local_N, local_I, params);

    // Run simulation steps
    for (int step = 0; step < params.SIMULATION_STEPS; step++)
    {
        // Move each individual and compute individuals to send to each node
        std::vector<std::vector<Individual>> local_individuals_by_group;
        local_individuals_by_group.resize(group_size);
        for (Individual &ind : local_individuals)
        {
            // Skip the nodes not belonging to the current node, unless it is the first step
            // in which the individuals have not been moved yet
            if (ind.parentNodeRank(params, group_size) != rank && step != 0)
            {
                continue;
            }

            ind.move(r_engine, params);

            for (int rank : ind.destinationRanks(params, group_size))
            {
                local_individuals_by_group[rank].push_back(ind);
            }
        }

        // Send and receive individuals
        std::vector<Individual> new_local_individuals;
        for (int targetNode = 0; targetNode < group_size; targetNode++)
        {
            int localIndividualsCount = local_individuals_by_group[targetNode].size();

            if (rank == targetNode)
            {
                // Gather count of the individuals to receive from each node
                std::vector<int> localCounts{};
                localCounts.resize(group_size);
                MPI_Gather(&localIndividualsCount, 1, MPI_INT, localCounts.data(), 1, MPI_INT, targetNode, MPI_COMM_WORLD);

                // Compute cumulative counts of the individuals to receive
                // from each node
                int new_individuals_count = 0;
                std::vector<int> displacements{};
                displacements.push_back(0);
                for (int i = 0; i < localCounts.size(); i++)
                {
                    new_individuals_count += localCounts[i];

                    if (i != localCounts.size() - 1)
                    {
                        displacements.push_back(displacements[i] + localCounts[i]);
                    }
                }
                new_local_individuals.resize(new_individuals_count);

                // Gather the individuals
                MPI_Gatherv(local_individuals_by_group[targetNode].data(), localIndividualsCount, MPI_Individual,
                            new_local_individuals.data(), localCounts.data(), displacements.data(), MPI_Individual, targetNode, MPI_COMM_WORLD);
                local_individuals = new_local_individuals;
            }
            else
            {
                MPI_Gather(&localIndividualsCount, 1, MPI_INT, nullptr, 1, MPI_INT, targetNode, MPI_COMM_WORLD);
                MPI_Gatherv(local_individuals_by_group[targetNode].data(), localIndividualsCount, MPI_Individual,
                            nullptr, nullptr, nullptr, MPI_Individual, targetNode, MPI_COMM_WORLD);
            }
        }

        // Update health status
        std::vector<bool> had_contacts{};
        had_contacts.resize(local_N);
        for (size_t i = 0; i < local_N; i++)
        {
            for (size_t j = 0; j < local_N; j++)
            {
                if (local_individuals[j].parentNodeRank(params, group_size) != rank)
                    continue;

                if (local_individuals[i].Health() == Individual::Infected &&
                    dist(local_individuals[i], local_individuals[j]) < params.SPREADING_DISTANCE)
                {
                    had_contacts[j] = true;
                }
            }
        }

        for (size_t i = 0; i < local_N; i++)
        {
            local_individuals[i].updateHealth(had_contacts[i], params);
        }

        // Compute statistics
        if ((step * params.TIME_STEP) % params.EXPORT_INTERVAL == 0)
        {
            logStatistics(local_individuals, params, rank, group_size, step * params.TIME_STEP);
        }

        if (params.SAVE_SNAPSHOTS)
        {
            saveSnapshot(local_individuals, rank, group_size, step, params);
        }
    }

    MPI_Finalize();
    return 0;
}