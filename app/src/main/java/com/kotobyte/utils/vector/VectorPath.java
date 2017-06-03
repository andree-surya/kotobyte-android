package com.kotobyte.utils.vector;

import java.util.List;

public class VectorPath {

    private List<VectorPathCommand> mCommands;

    VectorPath(List<VectorPathCommand> commands) {
        mCommands = commands;
    }

    public int getNumberOfCommands() {
        return mCommands.size();
    }

    public VectorPathCommand getCommand(int index) {
        return mCommands.get(index);
    }
}
