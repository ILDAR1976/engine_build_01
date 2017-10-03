package org.lwjglb.engine;

public interface IGameLogic {

    void init(EWindow EWindow) throws Exception;
    
    void input(EWindow EWindow, MouseInput mouseInput);

    void update(float interval, MouseInput mouseInput, EWindow EWindow);
    
    void render(EWindow EWindow);
    
    void cleanup();
}