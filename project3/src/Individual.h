#ifndef NSDS_PROJECT3_INDIVIDUAL
#define NSDS_PROJECT3_INDIVIDUAL
#include "Simulation.h"
#include <random>
#include <set>

class Individual
{
public:
    typedef enum
    {
        Susceptible,
        Infected,
        Recovered
    } State;
    static std::uniform_real_distribution<double> rand_uniform;

    friend float dist(Individual a, Individual b);

    void move(std::default_random_engine &r_engine, SimulationParameters &p);
    void updateHealth(bool had_contacts, SimulationParameters &p);
    State const &Health();

    Individual() = default;
    Individual(std::default_random_engine &r_engine, State health, SimulationParameters &p);
    int parentNodeRank(SimulationParameters &p, int size);
    std::set<int> destinationRanks(SimulationParameters &p, int size);

    // private:
    float x;
    float y;
    float direction;
    unsigned int counter;
    State health;
};

inline std::uniform_real_distribution<double> Individual::rand_uniform = std::uniform_real_distribution<double>(0, 1);

#endif
