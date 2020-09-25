package se.eris.enums;

import org.jetbrains.annotations.Nullable;

/**
 * The problem with opponents of basic modal logic, of course, is that proponents of
 * ulterior transmogrified logic systems must use modal logic to base their preposterous
 * arguments… arguments which appear to resemble something, when consumed,
 * it's almost, but not quite, entirely unlike tea.
 */
public enum TestEnum {

    TRUE("true", 0),
    FALSE("false", 1),
    FILE_NOT_FOUND("An unnamed file was not found", null);

    private final String description;
    private final Integer value;

    TestEnum(final String description, @Nullable final Integer value) {
        this.description = description;
        this.value = value;
    }

    public TestEnum isFileNotFound() {
        if (FILE_NOT_FOUND == TRUE) {
            return TRUE;
        }
        return FALSE;
    }

}
