package com.diligencia.app.movies;

/**
 * Created by OM1 on 12/16/15.
 */


 //@param 'V' the object type for data elements to be stored in the map.
public class FixedSizeHashMap<V>
{
    private String[] keys;
    private V[] items;

    private int tableSize;
    private int numElements;

    // -------------------------------------------------------------------------
    /**
     * Initializes a new HashTable object.
     *
     * @param size the maximum capacity of the table
     */
    @SuppressWarnings("unchecked")
    public FixedSizeHashMap(int size)
    {
        if (size < 0)
            throw new IllegalArgumentException("Invalid table size specified: " + size);

        numElements = 0;
        tableSize = size;
        keys = new String[size];
        items = (V[]) new Object[size];
    }

    // -------------------------------------------------------------------------
    /**
     * Inserts a key-value pair into the hash table.
     *
     * @param key the string associated with the generic object to be inserted
     * @param value the arbitrary data object associated with the key to be inserted.
     * @throws IllegalArgumentException if the key or value is NULL
     * @return true is insertion was successful, false otherwise
     */
    public boolean set(String key, V value)
    {
        if (key == null || value == null)
            throw new IllegalArgumentException("This implementation doesn't support support NULL keys or values!");

        // fixed-size, so don't allow insertion if the table is full
        // this also means that we don't need to rehash the table
        // NOTE: if we wanted to rehash the table, we'd do it here
        if (numElements >= tableSize)
            return false;

        int homeSlot = (key.hashCode() & 0x7FFFFFFF) % tableSize; // force sign bit to 0 so always +
        int originalSlot = homeSlot, counter = 0;

        // ideally we want to find an empty slot, but we need to watch for duplicate keys
        while (items[homeSlot] != null && !keyEquals(key, homeSlot))
        {
            if (counter++ <= tableSize / 2)
            {
                // let's use quadratic probing through N / 2 elements
                homeSlot = (originalSlot + counter * counter) % tableSize;
            }
            else
            {
                // we need to start at the initial home slot
                if (counter == tableSize / 2 + 1)
                    homeSlot = originalSlot;

                // now let's linear probe
                homeSlot = (homeSlot + 1) % tableSize;
            }
        }

        // now if a duplicate was detected, we will return false
        if (keyEquals(key, homeSlot))
            return false;

        items[homeSlot] = value;
        keys[homeSlot] = key;
        numElements++;

        return true;
    }

    // -------------------------------------------------------------------------
    /**
     * Retrieves the value associated with the specified key in the hash table.
     *
     * @param key the string associated with the generic object to be retrieved
     * @throws IllegalArgumentException if the key is NULL
     * @returns the value associated with the specified key, NULL otherwise
     */
    public V get(String key)
    {
        if (key == null)
            throw new IllegalArgumentException("This implementation doesn't support support NULL keys!");

        // empty table, no value to be found
        if (numElements == 0)
            return null;

        // find the index of the key, and return NULL if the key doesn't exist
        int correctSlot = getIndexOfKey(key);

        return correctSlot != -1 ? items[correctSlot] : null;
    }

    // ------------------------------------------------------------------------
    /**
     * Deletes a value from the table.
     *
     * @param key the string associated with the generic object to be deleted
     * @return the element deleted from the table, NULL otherwise
     */
    public V delete(String key)
    {
        if (key == null)
            throw new IllegalArgumentException("This implementation doesn't support support NULL keys!");

        // empty table, no value to find and delete
        if (numElements == 0)
            return null;

        // find the index of the key, and return NULL if the key doesn't exist
        int correctSlot = getIndexOfKey(key);
        if (correctSlot == -1)
            return null;

        // performance will seriously degrade if we don't rehash the table
        // specification says fixed-size... can do this here if need be

        V removedEntry = items[correctSlot];

        // remove the element
        items[correctSlot] = null;
        keys[correctSlot] = null;
        numElements--;

        return removedEntry;
    }

    // -------------------------------------------------------------------------
    /**
     * Calculates the correct index necessary for finding/deleting a key value
     * inside the table. This uses a hybrid approach of quadratic and linear
     * probing techniques. This algorithm will use quadratic probing up to
     * N / 2 elements, where it will then abandon this approach and switch
     * to linear probing from the initial hashed index.
     *
     * @param key the string associated with the generic object to be retrieved
     * @return the index of the key in the table, -1 if the key doesn't exist
     */
    public int getIndexOfKey(String key)
    {
        int homeSlot = (key.hashCode() & 0x7FFFFFFF) % tableSize;
        int originalSlot = homeSlot;
        int positionsChecked = 0, counter = 0;

        // we want to get the index without having to search through the entire table
        while (!keyEquals(key, homeSlot))
        {
            if (counter++ <= tableSize / 2)
            {
                // let's use quadratic probing through N / 2 elements
                homeSlot = (originalSlot + counter * counter) % tableSize;
            }
            else
            {
                // we need to start at the initial home slot
                if (counter == tableSize / 2 + 1)
                    homeSlot = originalSlot;

                // prevent an infinite loop if the element doesn't exist
                if (positionsChecked++ == tableSize)
                    return -1;

                homeSlot = (homeSlot + 1) % tableSize;
            }
        }
        return homeSlot;
    }

    // ------------------------------------------------------------------------
    /**
     * Checks to see if a string matches to a certain key already in the table.
     * No need for a tombstone marker because this the usage in this context
     * won't execute a sequence for null values.
     *
     * @param 'str' the string key to validate against the keys in the table
     * @param 'index' the index of the key to check the string against
     * @return whether or not the key is equal to the key located at the specific index
     */
    private boolean keyEquals(String key, int index)
    {
        return key.equals(keys[index]);
    }

    // -------------------------------------------------------------------------
    /**
     * Gets the load factor of the table indicated by the number of entries
     * in the table divided by the size of the table. This should never
     * return a value greater than 1.0 since the structure is fixed in size.
     *
     * @return the load factor of the table
     */
    public float load()
    {
        return (float)numElements / tableSize;
    }

    // -------------------------------------------------------------------------
    /**
     * Gets the maximum capacity of the table.
     * @return the maximum capacity of the table
     */
    public int size() { return tableSize; }

    // -------------------------------------------------------------------------
    /**
     * Gets the number of elements currently in the table.
     * @return the number of elements in the table
     */
    public int elements() { return numElements; }

    // ------------------------------------------------------------------------
    /**
     * Prints out the keys and values in the table.
     * NOTE: FOR TESTING PURPOSES ONLY
     */
    public void printTable()
    {
        for (int i = 0; i < tableSize; i++)
            System.out.println("<K, V>: [" + keys[i] + ", " + items[i] + "] at index " + i);
    }

}