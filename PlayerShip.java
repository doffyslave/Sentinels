package www.Sentinels.com;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerShip extends Ship {


    public PlayerShip(float movementSpeed, int shield, float width,
                      float height, float xCentre,
                      float yCentre, float laserWidth,
                      float laserHeight, float laserMovementSpeed,
                      float timeBetweenShots, TextureRegion shipTextureRegion,
                      TextureRegion shieldTextureRegion,
                      TextureRegion laserTextureRegion) {
        super(movementSpeed, shield, width, height, xCentre, yCentre, laserWidth, laserHeight, laserMovementSpeed, timeBetweenShots, shipTextureRegion, shieldTextureRegion, laserTextureRegion);
    }

    @Override
    public Laser[] fireLasers() {
        Laser[] laser = new Laser[2];
        laser[0] = new Laser(boundingBox.x + boundingBox.width*0.30f,boundingBox.y+boundingBox.height*0.45f,
            laserWidth,laserHeight,laserMovementSpeed,laserTextureRegion);
        laser[1] = new Laser(boundingBox.x + boundingBox.width*0.75f,boundingBox.y+boundingBox.height*0.45f,
            laserWidth,laserHeight,laserMovementSpeed,laserTextureRegion);

        timeSinceLastShot = 0;



        return laser;
    }
}
