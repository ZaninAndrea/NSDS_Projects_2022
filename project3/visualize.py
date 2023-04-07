import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation, PillowWriter

import re
import os


def plot_sir_curve():
    df = pd.read_csv("output.csv")
    plt.figure()
    plt.plot(df["time"], df["n_susceptible"], label="Susceptible")
    plt.plot(df["time"], df["n_infected"], label="Infected")
    plt.plot(df["time"], df["n_recovered"], label="Recovered")
    plt.ylabel("No. of agents")
    plt.xlabel("Time [days]")
    plt.legend()
    plt.savefig("output.png")


def plot_snapshots():
    rootDir = "./snapshots/"
    ls = os.listdir(rootDir)
    snapshots = [file for file in ls if re.match(r"snapshot_\d+\.csv", file)]
    snapshots.sort()
    snapshots = [snapshot for (i, snapshot) in enumerate(
        snapshots) if i % 3 == 0]

    fig, ax = plt.subplots()

    colors = ["#00ff00", "#ff0000", "#0000ff"]

    def animate(i):
        print("Animating step", i, "of", len(snapshots))
        df = pd.read_csv(rootDir+snapshots[i])
        ax.clear()
        ax.set_xlim(0, 10000)
        ax.set_ylim(0, 10000)
        sct = ax.scatter(df["x"], df["y"], c=[colors[int(x)]
                         for x in df["health"]], s=3)
        return sct,

    interval = 33
    ani = FuncAnimation(
        fig, animate, interval=interval, blit=True, repeat=True, frames=len(snapshots)
    )
    ani.save("sir.gif", dpi=100, writer=PillowWriter(fps=1000/interval))


if __name__ == "__main__":
    # plot_sir_curve()
    plot_snapshots()
