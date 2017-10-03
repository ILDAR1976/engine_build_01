package org.lwjglb.engine;

import org.lwjglb.engine.sound.SoundManager;

public class GameEngine implements Runnable {

    public static final int TARGET_FPS = 75;

    public static final int TARGET_UPS = 30;

    private final EWindow EWindow;

    private final Thread gameLoopThread;

    private final Timer timer;

    private final IGameLogic gameLogic;

    private final MouseInput mouseInput;

    private double lastFps;
    
    private int fps;
    
    private String EWindowTitle;
    
    public GameEngine(String EWindowTitle, boolean vSync, EWindow.EWindowOptions opts, IGameLogic gameLogic) throws Exception {
        this(EWindowTitle, 0, 0, vSync, opts, gameLogic);
    }

    public GameEngine(String EWindowTitle, int width, int height, boolean vSync, EWindow.EWindowOptions opts, IGameLogic gameLogic) throws Exception {
        this.EWindowTitle = EWindowTitle;
        gameLoopThread = new Thread(this, "GAME_LOOP_THREAD");
        EWindow = new EWindow(EWindowTitle, width, height, vSync, opts);
        mouseInput = new MouseInput();
        this.gameLogic = gameLogic;
        timer = new Timer();
    }

    public void start() {
        String osName = System.getProperty("os.name");
        if ( osName.contains("Mac") ) {
            gameLoopThread.run();
        } else {
            gameLoopThread.start();
        }
    }

    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (Exception excp) {
            excp.printStackTrace();
        } finally {
            cleanup();
        }
    }

    protected void init() throws Exception {
        EWindow.init();
        timer.init();
        mouseInput.init(EWindow);
        gameLogic.init(EWindow);
        lastFps = timer.getTime();
        fps = 0;
    }

    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        boolean running = true;
        while (running && !EWindow.WindowShouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            render();

            if ( !EWindow.isvSync() ) {
                sync();
            }
        }
    }

    protected void cleanup() {
        gameLogic.cleanup();
    }
    
    private void sync() {
        float loopSlot = 1f / TARGET_FPS;
        double endTime = timer.getLastLoopTime() + loopSlot;
        while (timer.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            }
        }
    }

    protected void input() {
        mouseInput.input(EWindow);
        gameLogic.input(EWindow, mouseInput);
    }

    protected void update(float interval) {
        gameLogic.update(interval, mouseInput, EWindow);
    }

    protected void render() {
        if ( EWindow.getEWindowOptions().showFps && timer.getLastLoopTime() - lastFps > 1 ) {
            lastFps = timer.getLastLoopTime();
            EWindow.setEWindowTitle(EWindowTitle + " - " + fps + " FPS");
            fps = 0;
        }
        fps++;
        gameLogic.render(EWindow);
        EWindow.update();
    }

}
