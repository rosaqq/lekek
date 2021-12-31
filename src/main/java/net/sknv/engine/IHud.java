package net.sknv.engine;

import net.sknv.engine.graph.IRenderable;

import java.util.ArrayList;

public interface IHud {

    ArrayList<IRenderable> getHudElements();

    default void cleanup() {
        ArrayList<IRenderable> HudElements = getHudElements();
        for (IRenderable elem : HudElements) {
            elem.cleanup();
        }
    }
}
