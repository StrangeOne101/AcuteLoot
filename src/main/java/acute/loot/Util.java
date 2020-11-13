package acute.loot;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public final class Util {

    private static final Random random = AcuteLoot.random;

    private Util() {
    }

    /**
     * Draw a random element from a list. If the list is empty, throw a NoSuchElementException.
     *
     * @param list the List to draw from
     * @return a random element from the list
     */
    public static <T> T drawRandom(List<T> list) {
        if (list.isEmpty()) throw new NoSuchElementException();
        return list.get(random.nextInt(list.size()));
    }

    public static List<Location> getLine(Location from, double distance, double addition) {
        //TODO: Take getLine out of other classes and use this one
        List<Location> locations = new ArrayList<>();
        final Vector direction = from.getDirection(); // End - Begin | length to 1
        for (double d = addition; d < distance; d += addition) {
            locations.add(from.clone().add(direction.clone().normalize().multiply(d)));
        }
        return locations;
    }

}
