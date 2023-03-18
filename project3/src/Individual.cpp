#include "Individual.h"
#include <math.h>
#include <random>
#include <stdio.h>
#include <vector>

Individual::Individual(std::default_random_engine &r_engine, State health, SimulationParameters &p)
{
    direction = rand_uniform(r_engine) * 2 * M_PI;
    x = rand_uniform(r_engine) * p.WORLD_WIDTH;
    y = rand_uniform(r_engine) * p.WORLD_HEIGHT;
    this->health = health;
}

int Individual::parentNodeRank(SimulationParameters &p, int size)
{
    int blockX = floor(this->x / p.BLOCK_WIDTH);
    int blockY = floor(this->y / p.BLOCK_HEIGHT);
    int blockN = blockX + blockY * p.HORIZONTAL_BLOCKS;

    return blockN % size;
}

void Individual::move(std::default_random_engine &r_engine, SimulationParameters &p)
{
    if (rand_uniform(r_engine) < 0.01)
    {
        direction = rand_uniform(r_engine) * 2 * M_PI;
    }

    x += cos(direction) * p.SPEED * p.TIME_STEP;
    y += sin(direction) * p.SPEED * p.TIME_STEP;

    // Pacman world boundaries
    if (x < 0)
    {
        x += p.WORLD_WIDTH;
    }
    else if (x > p.WORLD_WIDTH)
    {
        x -= p.WORLD_WIDTH;
    }

    if (y < 0)
    {
        y += p.WORLD_WIDTH;
    }
    else if (y > p.WORLD_WIDTH)
    {
        y -= p.WORLD_WIDTH;
    }
}

void Individual::updateHealth(bool had_contacts, SimulationParameters &p)
{
    switch (health)
    {
    case Susceptible:
        if (had_contacts)
        {
            counter += p.TIME_STEP;

            if (counter >= p.MIN_CONTACT_TIME)
            {
                health = Infected;
                counter -= p.MIN_CONTACT_TIME;
            }
        }
        else
        {
            counter = 0;
        }
        break;
    case Infected:
        counter += p.TIME_STEP;
        if (counter >= p.INFECTED_DURATION)
        {
            health = Recovered;
            counter -= p.INFECTED_DURATION;
        }
        break;
    case Recovered:
        counter += p.TIME_STEP;
        if (counter >= p.RECOVERED_DURATION)
        {
            health = Susceptible;
            counter -= p.RECOVERED_DURATION;
        }
        break;
    }
}

Individual::State const &Individual::Health()
{
    return this->health;
}