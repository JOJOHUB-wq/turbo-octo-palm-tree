package ua.atherium.hhevents.entities;

import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import net.minecraft.server.v1_16_R3.RandomPositionGenerator;
import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.Location;

import java.util.EnumSet;

public class PathfinderGoalRandomStrollInsideArena extends PathfinderGoal {

    protected final EntityCreature a;
    protected double b;
    protected double c;
    protected double d;
    protected final double e;
    private final Location center;
    private final int radius;

    public PathfinderGoalRandomStrollInsideArena(EntityCreature entity, double speed, Location center, int radius) {
        this.a = entity;
        this.e = speed;
        this.center = center;
        this.radius = radius;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
    }

    @Override
    public boolean a() {
        if (this.a.getVehicle() != null) {
            return false;
        } else if (this.a.getRandom().nextInt(120) != 0) {
            return false;
        } else {
            Vec3D vec3d = this.g();
            if (vec3d == null) {
                return false;
            } else {
                this.b = vec3d.x;
                this.c = vec3d.y;
                this.d = vec3d.z;
                return true;
            }
        }
    }

    protected Vec3D g() {
        Vec3D target = RandomPositionGenerator.a(this.a, 10, 7);
        if (target != null) {
            Location targetLoc = new Location(center.getWorld(), target.x, target.y, target.z);
            if (targetLoc.distanceSquared(center) <= radius * radius) {
                return target;
            } else {
                return new Vec3D(center.getX(), center.getY(), center.getZ());
            }
        }
        return null;
    }

    @Override
    public boolean b() {
        return !this.a.getNavigation().m();
    }

    @Override
    public void c() {
        this.a.getNavigation().a(this.b, this.c, this.d, this.e);
    }
}
