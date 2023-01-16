package dev.rosewood.roseloot.loot;

public enum OverwriteExisting {

    ALL,
    ITEMS,
    EXPERIENCE,
    NONE;

    public static OverwriteExisting combine(OverwriteExisting first, OverwriteExisting second) {
        if (first == ALL || second == ALL || (first == ITEMS && second == EXPERIENCE) || (first == EXPERIENCE && second == ITEMS)) return ALL;
        if ((first == ITEMS && second == ITEMS) || (first == ITEMS && second == NONE) || (first == NONE && second == ITEMS)) return ITEMS;
        if ((first == EXPERIENCE && second == EXPERIENCE) || (first == EXPERIENCE && second == NONE) || (first == NONE && second == EXPERIENCE)) return EXPERIENCE;
        if (first == NONE && second == NONE) return NONE;
        throw new IllegalStateException("Unexpected OverwriteExisting combination: " + first + ", " + second);
    }

    public static OverwriteExisting fromString(String name) {
        switch (name.toLowerCase()) {
            case "true" -> {
                return ALL;
            }
            case "false" -> {
                return NONE;
            }
        }

        for (OverwriteExisting value : values())
            if (value.name().equalsIgnoreCase(name))
                return value;
        return NONE;
    }

}
