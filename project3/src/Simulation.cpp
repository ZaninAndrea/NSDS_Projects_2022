#include "Simulation.h"
#include "Individual.h"
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

void logStatistics(std::vector<Individual> &local_individuals, int rank, int time)
{
    int local_susceptible = 0;
    int local_infected = 0;
    int local_recovered = 0;
    for (Individual &ind : local_individuals)
    {
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
        printf("Month: %02d, Day: %02d, Time %02d:%02d:%02d --- sus %d, inf %d, rec %d\n", date.month, date.day, date.hours, date.minutes, date.seconds, total_susceptible, total_infected, total_recovered);
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

int main(int argc, char *argv[])
{
    MPI_Init(&argc, &argv);
    int rank, group_size;
    MPI_Comm_size(MPI_COMM_WORLD, &group_size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    printf("Rankkk %d\n", rank);
    std::default_random_engine r_engine(rank);

    // Setup simulation parameters
    SimulationParameters params{};
    params.SPEED = 0.1;
    params.WORLD_WIDTH = 500.;
    params.WORLD_HEIGHT = 500.;
    params.HORIZONTAL_BLOCKS = 3;
    params.VERTICAL_BLOCKS = 3;
    params.BLOCK_WIDTH = params.WORLD_WIDTH / float(params.HORIZONTAL_BLOCKS);
    params.BLOCK_HEIGHT = params.WORLD_HEIGHT / float(params.VERTICAL_BLOCKS);
    params.TIME_STEP = 10;
    params.SIMULATION_STEPS = 1000000;
    params.SPREADING_DISTANCE = 15.;
    params.SPREADING_DISTANCE2 = params.SPREADING_DISTANCE * params.SPREADING_DISTANCE;
    params.INITIAL_INDIVIDUALS = 1000;
    params.INITIAL_INFECTED = 100;

    // Create MPI type for individual
    MPI_Datatype MPI_Individual;
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
        // Move each individual and then copy it in the pool of individuals
        // to send to its new parent node
        std::vector<std::vector<Individual>> local_individuals_by_group;
        local_individuals_by_group.resize(group_size);
        for (Individual &ind : local_individuals)
        {
            ind.move(r_engine, params);
            local_individuals_by_group[ind.parentNodeRank(params, group_size)].push_back(ind);
        }

        // Send the individuals to their new parent node
        std::vector<Individual> new_local_individuals;
        for (int targetNode = 0; targetNode < group_size; targetNode++)
        {
            int localIndividualsCount = local_individuals_by_group[targetNode].size();

            if (rank == targetNode)
            {
                std::vector<int> localCounts{};
                localCounts.resize(group_size);
                MPI_Gather(&localIndividualsCount, 1, MPI_INT, localCounts.data(), 1, MPI_INT, targetNode, MPI_COMM_WORLD);

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

                MPI_Gatherv(local_individuals_by_group[targetNode].data(), localIndividualsCount, MPI_Individual,
                            new_local_individuals.data(), localCounts.data(), displacements.data(), MPI_Individual, targetNode, MPI_COMM_WORLD);

                local_individuals = (new_local_individuals);
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
            for (size_t j = i + 1; j < local_N; j++)
            {
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
        if (step % 100 == 0)
            logStatistics(local_individuals, rank, step * params.TIME_STEP);
    }

    MPI_Finalize();
    return 0;
}