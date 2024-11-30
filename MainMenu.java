package www.Sentinels.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class MainMenu implements Screen {

    public static Random random = new Random();

    private Main game;
    private SpriteBatch batch;
    private Texture MenuTitle;
    private Texture NewGame;
    private Texture Background;
    private OrthographicCamera camera;
    private Viewport viewport;
    private static final float worldWidth = 1280;
    private static final float worldHeight = 720;
    private float buttonX;
    private float buttonY;
    private float titleX;
    private float titleY;
    private float fadeAlpha = 1.0f;
    private static final float fadeDuration = 2.0f;
    private Music MainTheme;
    public MainMenu(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        Background = new Texture("Ocean.jpg");
        MenuTitle = new Texture("Title.png");
        NewGame = new Texture("Playbutton.png");
        MainTheme = Gdx.audio.newMusic(Gdx.files.internal("Sound/Theme/aquatic-ambience.mp3"));
        camera = new OrthographicCamera();
        viewport = new FitViewport(worldWidth, worldHeight, camera);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector2 touchPos = viewport.unproject(new Vector2(screenX, screenY));
                float worldX = touchPos.x;
                float worldY = touchPos.y;
                if (worldX > buttonX && worldX < buttonX + NewGame.getWidth() &&
                    worldY > buttonY && worldY < buttonY + NewGame.getHeight()) {
                    game.setScreen(new GameScreen(game));
                }
                return true;
            }
        });

    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        MainTheme.play();
        MainTheme.setLooping(true);
        Gdx.gl.glClearColor(0,0,0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        titleX = (worldWidth - MenuTitle.getWidth()) / 2f;
        titleY = (worldHeight - MenuTitle.getHeight()) * 0.7f;
        buttonX = (worldWidth - NewGame.getWidth()) / 1.64f;
        buttonY = (worldHeight - NewGame.getHeight()) / 2.5f;
        game.batch.begin();
        game.batch.draw(Background,0,0,worldWidth,worldHeight);
        game.batch.draw(MenuTitle, titleX, titleY);
        game.batch.draw(NewGame, buttonX, buttonY, 150, 90);
        if (fadeAlpha > 0 ){
            fadeAlpha -= delta;
            fadeAlpha = Math.max(0, fadeAlpha);
            game.batch.setColor(0, 0, 0, fadeAlpha);
            game.batch.draw(MenuTitle, 0, 0, worldWidth, worldHeight);
            game.batch.setColor(1, 1, 1, 1);
        }
        game.batch.end();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(worldWidth / 2, worldHeight / 2, 0);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        MainTheme.stop();
    }

    @Override
    public void dispose() {
        MenuTitle.dispose();
        NewGame.dispose();
        batch.dispose();
        MainTheme.dispose();
    }
}
