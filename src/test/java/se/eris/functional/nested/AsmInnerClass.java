package se.eris.functional.nested;

import java.util.Objects;

class AsmInnerClass {
    final String name;
    private final String outerName;
    private final String innerName;
    private final int access;

    AsmInnerClass(final String name, final String outerName, final String innerName, final int access) {
        this.name = name;
        this.outerName = outerName;
        this.innerName = innerName;
        this.access = access;
    }

    @Override
    public String toString() {
        return "InnerClass{" +
                "name='" + name + '\'' +
                ", outerName='" + outerName + '\'' +
                ", innerName='" + innerName + '\'' +
                ", access=" + access +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AsmInnerClass that = (AsmInnerClass) o;

        if (access != that.access) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(outerName, that.outerName)) return false;
        return Objects.equals(innerName, that.innerName);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (outerName != null ? outerName.hashCode() : 0);
        result = 31 * result + (innerName != null ? innerName.hashCode() : 0);
        result = 31 * result + access;
        return result;
    }
}
