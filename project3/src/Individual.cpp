#include "Individual.h"
#include <math.h>
#include <random>
#include <set>
#include <stdio.h>

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

int getRankFromPosition(float x, float y, SimulationParameters &p, int size)
{
    // Ignore points outside the world
    if (x < 0 || x > p.WORLD_WIDTH || y < 0 || y > p.WORLD_HEIGHT)
    {
        return -1;
    }

    // Compute block coordinates
    int blockX = floor(x / p.BLOCK_WIDTH);
    int blockY = floor(y / p.BLOCK_HEIGHT);

    return (blockX + blockY * p.HORIZONTAL_BLOCKS) % size;
}

std::set<int> Individual::destinationRanks(SimulationParameters &p, int size)
{
    std::set<int> ranks;
    ranks.insert(getRankFromPosition(this->x, this->y, p, size));
    ranks.insert(getRankFromPosition(this->x + p.SPREADING_DISTANCE, this->y, p, size));
    ranks.insert(getRankFromPosition(this->x - p.SPREADING_DISTANCE, this->y, p, size));
    ranks.insert(getRankFromPosition(this->x, this->y + p.SPREADING_DISTANCE, p, size));
    ranks.insert(getRankFromPosition(this->x, this->y - p.SPREADING_DISTANCE, p, size));
    ranks.insert(getRankFromPosition(this->x + p.SPREADING_DISTANCE, this->y + p.SPREADING_DISTANCE, p, size));
    ranks.insert(getRankFromPosition(this->x - p.SPREADING_DISTANCE, this->y + p.SPREADING_DISTANCE, p, size));
    ranks.insert(getRankFromPosition(this->x + p.SPREADING_DISTANCE, this->y - p.SPREADING_DISTANCE, p, size));
    ranks.insert(getRankFromPosition(this->x - p.SPREADING_DISTANCE, this->y - p.SPREADING_DISTANCE, p, size));

    ranks.erase(-1);

    return ranks;
}

void Individual::move(std::default_random_engine &r_engine, SimulationParameters &p)
{
    if (rand_uniform(r_engine) < 0.01)
    {
        direction = rand_uniform(r_engine) * 2 * M_PI;
    }

    x += cos(direction) * p.SPEED * p.TIME_STEP;
    y += sin(direction) * p.SPEED * p.TIME_STEP;

    // Bounce off the walls
    if (x < 0)
    {
        x = -x;
        direction = M_PI - direction;
    }
    else if (x > p.WORLD_WIDTH)
    {
        x = p.WORLD_WIDTH - (x - p.WORLD_WIDTH);
        direction = M_PI - direction;
    }

    if (y < 0)
    {
        y = -y;
        direction = -direction;
    }
    else if (y > p.WORLD_WIDTH)
    {
        y = p.WORLD_HEIGHT - (y - p.WORLD_HEIGHT);
        direction = -direction;
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

bool Individual::isIn(Country &country)
{
    return this->x >= country.minX && this->x <= country.maxX && this->y >= country.minY && this->y <= country.maxY;
}