package org.mangorage.mangobot.modules.betatricks;

import com.google.gson.annotations.Expose;

public class BetaTrick {
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
    private String content;

    @Expose
    private boolean suppress = false;

    // Used for Script Based Tricks
    @Expose
    private String script;

    // Used for Alias Based Tricks
    @Expose
    private String aliasTarget;

    @Expose
    private final String trickID;

    @Expose
    private final String guildID;

    @Expose
    private final long created;

    @Expose
    private TrickType type;

    protected BetaTrick(String trickID, String guildID) {
        this.trickID = trickID;
        this.guildID = guildID;
        this.created = System.currentTimeMillis();
    }

    protected void setAliasTarget(String target) {
        this.aliasTarget = target;
    }
    protected void setSuppress(boolean suppress) {
        this.suppress = suppress;
    }

    protected void setType(TrickType type) {
        this.type = type;
    }

    protected void setContent(String content) {
        this.content = content;
    }

    protected void setScript(String script) {
        this.script = script;
    }

    protected void setOwnerID(long ownerID) {
        this.ownerID = ownerID;
    }

    protected void setLastUserEdited(long user) {
        this.lastUserEdited = user;
    }

    protected void setLock(boolean locked) {
        this.locked = locked;
    }

    protected void setLastEdited(long ms) {
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

    public String getGuildID() {
        return guildID;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isSuppressed() {
        return suppress;
    }

    protected void use() {
        timesUsed++;
    }
}
