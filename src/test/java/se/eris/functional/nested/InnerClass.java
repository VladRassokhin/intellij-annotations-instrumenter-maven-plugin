package se.eris.functional.nested;

class InnerClass {
    public String name;
    public String outerName;
    public String innerName;
    int access;

    public InnerClass(final String name, final String outerName, final String innerName, final int access) {
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

        final InnerClass that = (InnerClass) o;

        if (access != that.access) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (outerName != null ? !outerName.equals(that.outerName) : that.outerName != null) return false;
        return innerName != null ? innerName.equals(that.innerName) : that.innerName == null;
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
