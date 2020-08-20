package me.devvy.dodgebolt.game;

public enum DodgeboltGameState {

    WAITING,
    PREGAME_COUNTDOWN,
    INGAME,
    INTERMISSION;

    public static boolean isGameRunning(DodgeboltGameState state) {
        return state != WAITING;
    }

}
