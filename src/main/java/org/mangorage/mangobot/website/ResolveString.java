package org.mangorage.mangobot.website;

public record ResolveString(String value) {
    public ResolveString resolve(String value) {
        return new ResolveString(STR."\{this.value}/\{value}");
    }

    public String resolveFully(String value) {
        return STR."\{this.value}/\{value}";
    }
}
