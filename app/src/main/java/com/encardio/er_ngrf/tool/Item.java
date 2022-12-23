package com.encardio.er_ngrf.tool;

/**
 * @author Sandeep
 * <p>
 * A POJO that contains some properties to show in the list.
 */
public class Item implements Comparable<Item> {
    /**
     * The id.
     */
    private long id;
    /**
     * The file.
     */
    private final String file;

    /**
     * Instantiates a new item.
     *
     * @param id   the id
     * @param file the file
     */
    public Item(long id, String file) {
        super();
        this.id = id;
        this.file = file;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * Comparable interface implementation.
     *
     * @param other the other
     * @return the int
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Item other) {
        return (int) (id - other.getId());
    }
}
