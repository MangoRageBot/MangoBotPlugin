package org.mangorage.mangobotplugin.commands.trick;

import com.google.gson.annotations.Expose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.mangorage.mangobotcore.api.util.data.FileName;
import org.mangorage.mangobotcore.api.util.data.IFileNameResolver;

import java.util.UUID;

@Entity
@Table(name = "tricks")
public final class Trick implements IFileNameResolver {

    @Id
    private UUID uuid;

    @Expose
    private long ownerID;

    @Expose
    private long lastUserEdited;

    @Expose
    private boolean locked = false;

    @Expose
    private long lastEdited;

    @Expose
    private long timesUsed = 0;

    // Cant be used for Script Based Tricks
    @Expose
    @Column(columnDefinition = "TEXT")
    private String content;

    @Expose
    private boolean suppress = false;

    // Used for Script Based Tricks
    @Expose
    @Column(columnDefinition = "TEXT")
    private String script;

    // Used for Alias Based Tricks
    @Expose
    @Column(columnDefinition = "TEXT")
    private String aliasTarget;

    @Expose
    @Column(columnDefinition = "TEXT")
    private String trickID;

    @Expose
    private long guildID;

    @Expose
    private long created;

    @Expose
    private TrickType type;

    private Trick() {}

    public Trick(String trickID, long guildID) {
        this.uuid = UUID.randomUUID();
        this.trickID = trickID;
        this.guildID = guildID;
        this.created = System.currentTimeMillis();
    }

    public void setAliasTarget(String target) {
        this.aliasTarget = target;
    }

    public void setSuppress(boolean suppress) {
        this.suppress = suppress;
    }

    public void setType(TrickType type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setOwnerID(long ownerID) {
        this.ownerID = ownerID;
    }

    public void setLastUserEdited(long user) {
        this.lastUserEdited = user;
    }

    public void setLock(boolean locked) {
        this.locked = locked;
    }

    public void setLastEdited(long ms) {
        this.lastEdited = ms;
    }

    public TrickType getType() {
        return type;
    }

    public String getAliasTarget() {
        return aliasTarget;
    }

    public long getOwnerID() {
        return ownerID;
    }

    public long getLastUserEdited() {
        return lastUserEdited;
    }

    public long getLastEdited() {
        return lastEdited;
    }

    public long getTimesUsed() {
        return timesUsed;
    }

    public String getContent() {
        return content;
    }

    public String getScript() {
        return script;
    }

    public String getTrickID() {
        return trickID;
    }

    public long getCreated() {
        return created;
    }

    public long getGuildID() {
        return guildID;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isSuppressed() {
        return suppress;
    }

    public void use() {
        timesUsed++;
    }

    @Override
    public FileName resolve() {
        return new FileName(guildID + "", trickID);
    }
}
