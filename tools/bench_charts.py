#!/usr/bin/env python3
"""Draw the benchmark bar charts in docs/assets/bench/ from couchBench result files.

usage: bench_charts.py <outdir> vanilla=a.jsonl,b.jsonl sodium=c.jsonl ...  (several files per config -> median)
"""
import json, statistics, sys
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

TASKS = [("overworld-idle", "Overworld\nstanding still"), ("overworld-explore", "Overworld\nexploring"),
         ("nether-idle", "Nether\nstanding still"), ("nether-explore", "Nether\nexploring")]
RAMP = ["#86b6ef", "#3987e5", "#1c5cab", "#0d366b"]  # 1p..4p, light to dark
SURFACE, INK, MUTED, GRID = "#fcfcfb", "#0b0b0b", "#52514e", "#e4e3df"


def load(path):
    return {d["label"]: d["fps"] for d in map(json.loads, open(path)) if "fps" in d}


def median_fps(files):
    runs = [load(f) for f in files]
    return {k: statistics.median(r[k] for r in runs) for k in runs[0]}


def draw(ax, data, title, ymax):
    w = 0.19
    for i, (task, _) in enumerate(TASKS):
        for p in range(4):
            v = data[f"{p + 1}p.{task}"]
            x = i + (p - 1.5) * (w + 0.015)
            ax.bar(x, v, w, color=RAMP[p], label=f"{p + 1} player" + ("s" if p else ""), zorder=3,
                   linewidth=0) if i == 0 else ax.bar(x, v, w, color=RAMP[p], zorder=3, linewidth=0)
            ax.text(x, v * 1.07, f"{v:.0f}", ha="center", va="bottom", fontsize=7.5, color=MUTED)
    ax.set_yscale("log")
    ax.set_ylim(20, ymax)
    ax.set_xticks(range(len(TASKS)), [t for _, t in TASKS], color=INK, fontsize=9.5)
    ax.set_ylabel("frames per second (log scale)", color=MUTED, fontsize=9)
    ax.set_title(title, loc="left", color=INK, fontsize=12, fontweight="bold")
    ax.set_facecolor(SURFACE)
    ax.grid(axis="y", color=GRID, zorder=0)
    ax.tick_params(colors=MUTED, length=0)
    for s in ax.spines.values():
        s.set_visible(False)


def figure(path, panels, ymax):
    fig, axes = plt.subplots(len(panels), 1, figsize=(9.5, 3.9 * len(panels)), facecolor=SURFACE)
    for ax, (title, data) in zip(axes, panels):
        draw(ax, data, title, ymax)
    axes[0].legend(ncol=4, loc="upper right", frameon=False, fontsize=9, labelcolor=INK)
    fig.tight_layout(h_pad=2.2)
    fig.savefig(path, dpi=140, facecolor=SURFACE)


if __name__ == "__main__":
    out = sys.argv[1]
    cfg = {k: median_fps(v.split(",")) for k, v in (a.split("=") for a in sys.argv[2:])}
    figure(f"{out}/vanilla-vs-sodium.png",
           [("Vanilla rendering", cfg["vanilla"]), ("With Sodium", cfg["sodium"])], 8000)
    if "dh" in cfg:
        figure(f"{out}/distant-horizons.png",
               [("Distant Horizons", cfg["dh"]), ("Distant Horizons + Sodium", cfg["sodium-dh"])], 2000)
