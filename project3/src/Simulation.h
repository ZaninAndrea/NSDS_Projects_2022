#ifndef NSDS_PROJECT3_SIMULATION
#define NSDS_PROJECT3_SIMULATION
#include <string>
#include <vector>

struct Country
{
    float minX;
    float maxX;
    float minY;
    float maxY;
    std::string name;
};

struct SimulationParameters
{
    float SPEED;
    float WORLD_WIDTH;
    float WORLD_HEIGHT;
    float BLOCK_WIDTH;
    float BLOCK_HEIGHT;
    int HORIZONTAL_BLOCKS;
    int VERTICAL_BLOCKS;
    unsigned int TIME_STEP = 60.;
    unsigned int SIMULATION_STEPS;
    float SPREADING_DISTANCE;
    float SPREADING_DISTANCE2;
    unsigned int INITIAL_INDIVIDUALS;
    unsigned int INITIAL_INFECTED;
    unsigned int MIN_CONTACT_TIME = 10 * 60;
    unsigned int INFECTED_DURATION = 3600 * 24 * 10;
    unsigned int RECOVERED_DURATION = 3600 * 24 * 90;
    unsigned int EXPORT_INTERVAL = 60 * 60 * 24;
    bool SAVE_SNAPSHOTS = false;
    std::vector<Country> COUNTRIES;
};

#endif