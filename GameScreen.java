package www.Sentinels.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

public class GameScreen implements Screen {

    private Main game;
    private TextureAtlas CharAtlas;
    private Animation<TextureRegion> CharAnimation;
    private Camera camera;
    private Viewport viewport;

    private SpriteBatch batch;
    private TextureAtlas textureAtlas;
    private  Texture explosionTexture;

    private TextureRegion[] backgrounds;

    private TextureRegion playerShipTextureRegion, playerShieldTextureRegion, enemyShipTextureRegion,
        enemyShieldTextureRegion, playerLaserTextureRegion, enemyLaserTextureregion;

    private float[] backgroundOffsets = {0, 0, 0, 0, 0};
    private float backgroundMaxScrollingSpeed;
    private  float timeBetweenEnemySpawns = 3f;
    private float enemySpawnerTimer = 0;

    // World parameters
    private final int WORLD_WIDTH = 72;
    private final int WORLD_HEIGHT = 128;
    private final float TOUCH_MOVEMENT_THRESHOLD = 0.5f;


    //game Objects
    private PlayerShip playerShip;
    private LinkedList<enemyShip> enemyShipList;
    private LinkedList<Laser> playerLaserList;
    private LinkedList<Laser> enemyLaserList;
    private LinkedList<Explosion> explosionList;



    public GameScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        //set up the texture atlas
        TextureAtlas mainAtlas = new TextureAtlas("main.atlas");
        TextureAtlas enemyAtlas = new TextureAtlas("enemy.atlas");
        TextureAtlas shipsAtlas = new TextureAtlas("ships.atlas");


        // Initialize the backgrounds array
        backgrounds = new TextureRegion[5];

        TextureAtlas SpriteAtlas;
        backgrounds[0] = mainAtlas.findRegion("backg2");
        backgrounds[1] = mainAtlas.findRegion("backg1");
        backgrounds[2] = mainAtlas.findRegion("backg3");
        backgrounds[3] = mainAtlas.findRegion("backg4");
        backgrounds[4] = mainAtlas.findRegion("backg5");

        int backgroundHeight = WORLD_HEIGHT * 2;
        backgroundMaxScrollingSpeed = (float) (WORLD_HEIGHT) / 4;


        //initialize texture regions
        playerShipTextureRegion = shipsAtlas.findRegion("Submarine");
        enemyShipTextureRegion = enemyAtlas.findRegion("Enemyship");
        playerShieldTextureRegion = shipsAtlas.findRegion("shield1");
        playerShieldTextureRegion.flip(false, true);
        enemyShieldTextureRegion = shipsAtlas.findRegion("shield1");
        playerShieldTextureRegion.flip(false, true);
        playerLaserTextureRegion = shipsAtlas.findRegion("attack");
        enemyLaserTextureregion = enemyAtlas.findRegion("laserRed07");

        if (playerShipTextureRegion == null || enemyShipTextureRegion== null) {
            Gdx.app.log("TextureRegion Error", "playerShipTextureRegion not found in mainAtlas");
        }

         explosionTexture = new Texture("blood.png");

        //set up game objects
        playerShip = new PlayerShip(43,6,10,20,
        WORLD_WIDTH/2, WORLD_HEIGHT/4, 3.50f, 10, 45, 0.5f,
            playerShipTextureRegion,playerShieldTextureRegion,playerLaserTextureRegion);

        enemyShipList = new LinkedList<>();


        playerLaserList = new LinkedList<>();
        enemyLaserList = new LinkedList<>();

        explosionList = new LinkedList<>();

        batch = new SpriteBatch();
    }

    @Override
    public void show() {

    }
    @Override
    public void render(float deltaTime) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        // Render scrolling background
        renderBackground(deltaTime);

        detectInput (deltaTime);
        playerShip.update(deltaTime);

        spawnEnemyShips(deltaTime);

        ListIterator<enemyShip> enemyShipListIterator = enemyShipList.listIterator();
        while (enemyShipListIterator.hasNext()) {
            enemyShip enemyShip = enemyShipListIterator.next();
            moveEnemy(enemyShip, deltaTime);
            enemyShip.update(deltaTime);

            //enemy ships
            enemyShip.draw(batch);

        }
        //player ships
        playerShip.draw(batch);

        //lasers
        renderLasers(deltaTime);

        //detect collision between lasers and ships
        detectCollisions();

        //explosions
        renderExplosions(deltaTime);

        batch.end();
    }

    private void spawnEnemyShips (float deltaTime) {
        enemySpawnerTimer += deltaTime;

        if (enemySpawnerTimer > timeBetweenEnemySpawns) {
            enemyShipList.add(new enemyShip(39, 5, 10, 25,
                MainMenu.random.nextFloat() * (WORLD_WIDTH - 10) + 5, WORLD_HEIGHT - 5, 1.3f, 5, 50, 0.8f,
                enemyShipTextureRegion, enemyShieldTextureRegion, enemyLaserTextureregion));
            enemySpawnerTimer -= timeBetweenEnemySpawns;
        }
    }

    private void detectInput (float deltaTime) {

        //kboard input

        //strategy : determine the max distance the ship can move
        //check each key that matters and move accordingly

        float leftLimit, rightLimit,upLimit, downLimit;
        leftLimit = -playerShip.boundingBox.x;
        downLimit = -playerShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - playerShip.boundingBox.x - playerShip.boundingBox.width;
        upLimit = (float) WORLD_HEIGHT/2 - playerShip.boundingBox.y - playerShip.boundingBox.height;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT ) && rightLimit > 0 ) {
            playerShip.translate(Math.min(playerShip.movementSpeed*deltaTime, rightLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP ) && upLimit > 0 ) {
            playerShip.translate( 0f, Math.min(playerShip.movementSpeed*deltaTime, upLimit));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT ) && leftLimit < 0 ) {
            playerShip.translate(Math.max(-playerShip.movementSpeed*deltaTime, leftLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && downLimit < 0 ) {
            playerShip.translate(0f, Math.max(-playerShip.movementSpeed*deltaTime, downLimit));
        }

        //touch input (and mouse?)
        if (Gdx.input.isTouched()); {
            //get the screen position of the touch
            float xTouchPixels = Gdx.input.getX();
            float yTouchPixels = Gdx.input.getY();

            //convert to world position
            Vector2 touchPoint = new Vector2(xTouchPixels,yTouchPixels);
            touchPoint = viewport.unproject(touchPoint);

            //calculate the x and y
            Vector2 playerShipCentre = new Vector2(playerShip.boundingBox.x + playerShip.boundingBox.width/2,
                playerShip.boundingBox.y + playerShip.boundingBox.height/2);

            float touchDistance = touchPoint.dst(playerShipCentre);

            if (touchDistance > TOUCH_MOVEMENT_THRESHOLD) {
                float xTouchDifference = touchPoint.x - playerShipCentre.x;
                float yTouchDifference = touchPoint.y - playerShipCentre.y;

                //scale to the maximum speed of the ship
                float xMove = xTouchDifference / touchDistance * playerShip.movementSpeed * deltaTime;
                float yMove = yTouchDifference / touchDistance * playerShip.movementSpeed * deltaTime;

                if ( xMove > 0 ) xMove = Math.min(xMove, rightLimit);
                else xMove = Math.max (xMove,leftLimit);

                if ( yMove > 0 ) yMove = Math.min(yMove, upLimit);
                else yMove = Math.max (yMove,downLimit);

                playerShip.translate(xMove,yMove);

            }
        }


    }

    private  void moveEnemy( enemyShip enemyShip, float deltaTime) {
        //strategy : determine the max distance the ship can move

        float leftLimit, rightLimit,upLimit, downLimit;
        leftLimit = -enemyShip.boundingBox.x;
        downLimit = (float) WORLD_HEIGHT/2 -enemyShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - enemyShip.boundingBox.x - enemyShip.boundingBox.width;
        upLimit = WORLD_HEIGHT - enemyShip.boundingBox.y - enemyShip.boundingBox.height;

        float xMove = enemyShip.getDirectionVector().x * enemyShip.movementSpeed * deltaTime;
        float yMove = enemyShip.getDirectionVector().y * enemyShip.movementSpeed * deltaTime;

        if ( xMove > 0 ) xMove = Math.min(xMove, rightLimit);
        else xMove = Math.max (xMove,leftLimit);

        if ( yMove > 0 ) yMove = Math.min(yMove, upLimit);
        else yMove = Math.max (yMove,downLimit);

        enemyShip.translate(xMove,yMove);

    }

    private void detectCollisions () {

        // for each player laser,  check whether it intersects an enemy ship
        ListIterator<Laser> laserListIterator = playerLaserList.listIterator();
        while (laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            ListIterator<enemyShip> enemyShipListIterator = enemyShipList.listIterator();
            while (enemyShipListIterator.hasNext()) {
                enemyShip enemyShip = enemyShipListIterator.next();

            if (enemyShip.intersects(laser.boundingBox)) {
                // contact with enemy ship
                if (enemyShip.hitAndCheckDestroyed(laser)) {
                    enemyShipListIterator.remove();
//                    explosionList.add(new Explosion(explosionTexture, new Rectangle(enemyShip.boundingBox),
//                        0.7f));
                }
                laserListIterator.remove();
                break;
            }
            }
        }

        //for each enemy laser, check whether it intersects the player ship
        laserListIterator = enemyLaserList.listIterator();
        while (laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            if (playerShip.intersects(laser.boundingBox)) {
                // contact with player ship
                playerShip.hitAndCheckDestroyed(laser);
                laserListIterator.remove();

            }
        }



    }

    private void renderExplosions (float deltaTime) {
        ListIterator<Explosion> explosionListIterator = explosionList.listIterator();
        while (explosionListIterator.hasNext()) {
            Explosion explosion = explosionListIterator.next();
            if (explosion.isFinished()) {
                explosionListIterator.remove();
            }
            else {
                explosion.draw(batch);
            }
        }
    }

    private void renderLasers(float deltaTime) {
        //create news lase
        //player lasers
        if (playerShip.canFireLaser()) {
            Laser[] lasers = playerShip.fireLasers();
            for (Laser laser: lasers) {
                playerLaserList.add (laser);
            }
        }
        //enemy lasers
        ListIterator<enemyShip> enemyShipListIterator = enemyShipList.listIterator();
        while (enemyShipListIterator.hasNext()) {
             enemyShip enemyShip = enemyShipListIterator.next();
            if (enemyShip.canFireLaser()) {
                Laser[] lasers = enemyShip.fireLasers();
                enemyLaserList.addAll(Arrays.asList(lasers));
            }
        }
        //draw lasers
        //remove old lasers
        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y += laser.movementSpeed*deltaTime;
            if (laser.boundingBox.y > WORLD_HEIGHT) {
                iterator.remove();
            }
        }

        iterator = enemyLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y -= laser.movementSpeed*deltaTime;
            if (laser.boundingBox.y + laser.boundingBox.height < 0 ) {
                iterator.remove();
            }
        }
    }

    private void renderBackground(float deltaTime) {
        backgroundOffsets[0] += deltaTime * backgroundMaxScrollingSpeed / 8;
        backgroundOffsets[1] += deltaTime * backgroundMaxScrollingSpeed / 8;
        backgroundOffsets[2] += deltaTime * backgroundMaxScrollingSpeed / 4;
        backgroundOffsets[3] += deltaTime * backgroundMaxScrollingSpeed / 2;
        backgroundOffsets[4] += deltaTime * backgroundMaxScrollingSpeed;

        for (int layer = 0; layer < backgroundOffsets.length; layer++) {
            if (backgroundOffsets[layer] > WORLD_HEIGHT) {
                backgroundOffsets[layer] = 0;
            }
            batch.draw(backgrounds[layer],
                0,
                -backgroundOffsets[layer], WORLD_WIDTH, WORLD_HEIGHT);
            batch.draw(backgrounds[layer],
                0,
                -backgroundOffsets[layer] + WORLD_HEIGHT, WORLD_WIDTH, WORLD_HEIGHT);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        for (TextureRegion region : backgrounds) {
            if (region != null && region.getTexture() != null) {
                region.getTexture().dispose();
            }
        }

    }

    }


