package ua.atherium.hhevents.entities;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

public class CustomGolem extends EntityIronGolem {

    private final Location centerPoint;
    private final int radius;

    public CustomGolem(Location loc, int radius) {
        super(EntityTypes.IRON_GOLEM, ((CraftWorld) loc.getWorld()).getHandle());
        this.centerPoint = loc;
        this.radius = radius;
        this.setPosition(loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    protected void initPathfinder() {
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalRandomStrollInsideArena(this, 1.0D, centerPoint, radius));
        this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource == DamageSource.STUCK || damagesource == DamageSource.CRAMMING || damagesource == DamageSource.FALL) {
            return false;
        }
        return super.damageEntity(damagesource, f);
    }
}
