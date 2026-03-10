package ua.atherium.hhevents.events;

import org.bukkit.Location;

public abstract class BaseEvent {

    protected Location location;
    protected EventStatus status;

    public BaseEvent() {
        this.status = EventStatus.WAITING;
    }

    public abstract void start();

    public abstract void stop();

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public enum EventStatus {
        WAITING,
        ACTIVE,
        FINISHED
    }
}
