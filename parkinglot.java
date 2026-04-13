import java.util.ArrayList;
import java.util.List;

// ==========================================
// MODELS (Entities)
// ==========================================

enum VehicleSize {
    MOTORCYCLE, COMPACT, LARGE
}

// Abstract Base Class for all Vehicles
abstract class Vehicle {
    protected String licensePlate;
    protected int spotsNeeded;
    protected VehicleSize size;

    public Vehicle(String plate, int spots, VehicleSize size) {
        this.licensePlate = plate;
        this.spotsNeeded = spots;
        this.size = size;
    }

    public int getSpotsNeeded() {
        return spotsNeeded;
    }

    public VehicleSize getSize() {
        return size;
    }

    public String getPlate() {
        return licensePlate;
    }

    // Abstract method to force subclasses to define how they print
    public abstract void print();
}

class Motorcycle extends Vehicle {
    public Motorcycle(String plate) {
        super(plate, 1, VehicleSize.MOTORCYCLE);
    }

    @Override
    public void print() {
        System.out.print("🏍️  Motorcycle [" + licensePlate + "]");
    }
}

class Car extends Vehicle {
    public Car(String plate) {
        super(plate, 1, VehicleSize.COMPACT);
    }

    @Override
    public void print() {
        System.out.print("🚗 Car [" + licensePlate + "]");
    }
}

class Bus extends Vehicle {
    // A bus needs 5 consecutive large spots
    public Bus(String plate) {
        super(plate, 5, VehicleSize.LARGE);
    }

    @Override
    public void print() {
        System.out.print("🚌 Bus [" + licensePlate + "]");
    }
}

// ==========================================
// INFRASTRUCTURE
// ==========================================

class ParkingSpot {
    private Vehicle vehicle; // Pointer to the vehicle parked here (if any)
    private VehicleSize spotSize;
    private int row;
    private int spotNumber;

    public ParkingSpot(int r, int n, VehicleSize sz) {
        this.vehicle = null;
        this.row = r;
        this.spotNumber = n;
        this.spotSize = sz;
    }

    public boolean isAvailable() {
        return vehicle == null;
    }

    // Checking if a specific vehicle CAN fit in this specific spot
    public boolean canFitVehicle(Vehicle v) {
        if (!isAvailable())
            return false;

        // Motorcycle fits in any spot
        if (v.getSize() == VehicleSize.MOTORCYCLE)
            return true;

        // Car (Compact) fits in Compact or Large
        if (v.getSize() == VehicleSize.COMPACT) {
            return spotSize == VehicleSize.COMPACT || spotSize == VehicleSize.LARGE;
        }

        // Bus (Large) ONLY fits in Large spots
        return spotSize == VehicleSize.LARGE;
    }

    public boolean park(Vehicle v) {
        if (!canFitVehicle(v))
            return false;
        vehicle = v;
        return true;
    }

    public void removeVehicle() {
        vehicle = null;
    }

    public void print() {
        if (!isAvailable()) {
            System.out.print("[ X ] "); // Occupied
        } else {
            if (spotSize == VehicleSize.LARGE)
                System.out.print("[ L ] ");
            else if (spotSize == VehicleSize.COMPACT)
                System.out.print("[ C ] ");
            else
                System.out.print("[ M ] ");
        }
    }
}

class Level {
    private int floor;
    private List<ParkingSpot> spots = new ArrayList<>();
    private int availableSpots;
    private static final int SPOTS_PER_ROW = 10;

    public Level(int flr, int numSpots) {
        this.floor = flr;
        this.availableSpots = numSpots;
        // Simple assignment: half compact, half large
        for (int i = 0; i < numSpots; ++i) {
            VehicleSize sz = (i < numSpots / 2) ? VehicleSize.COMPACT : VehicleSize.LARGE;
            spots.add(new ParkingSpot(i / SPOTS_PER_ROW, i, sz));
        }
    }

    // Attempt to park a vehicle on this level.
    // Handles finding consecutive spots for buses!
    public boolean parkVehicle(Vehicle v) {
        if (availableSpots < v.getSpotsNeeded())
            return false;

        int spotsNeeded = v.getSpotsNeeded();
        int consecutiveSpots = 0;
        int startIndex = -1;

        for (int i = 0; i < spots.size(); ++i) {
            if (spots.get(i).canFitVehicle(v)) {
                if (startIndex == -1)
                    startIndex = i;
                consecutiveSpots++;

                if (consecutiveSpots == spotsNeeded) {
                    // We found enough space! Park them.
                    for (int j = startIndex; j <= i; ++j) {
                        spots.get(j).park(v);
                    }
                    availableSpots -= spotsNeeded;
                    return true;
                }
            } else {
                // Streak broken, reset
                consecutiveSpots = 0;
                startIndex = -1;
            }
        }
        return false;
    }

    public void print() {
        System.out.print("Floor " + floor + ": ");
        for (int i = 0; i < spots.size(); ++i) {
            spots.get(i).print();
            if ((i + 1) % SPOTS_PER_ROW == 0)
                System.out.print("\n         ");
        }
        System.out.println();
    }
}

// ==========================================
// THE FACADE (Main System)
// ==========================================
class ParkingLotFacade {
    private List<Level> levels = new ArrayList<>();

    public ParkingLotFacade(int numLevels, int spotsPerLevel) {
        for (int i = 0; i < numLevels; ++i) {
            levels.add(new Level(i, spotsPerLevel));
        }
    }

    // The client only interacts with this simple method!
    public boolean parkVehicle(Vehicle v) {
        System.out.print("Attempting to park ");
        v.print();
        System.out.println("...");

        for (Level level : levels) {
            if (level.parkVehicle(v)) {
                System.out.println("-> Successfully parked!");
                return true;
            }
        }
        System.out.println("-> Lot is Full. Cannot park.");
        return false;
    }

    public void print() {
        System.out.println("--- Parking Lot Status ---");
        for (Level level : levels) {
            level.print();
        }
    }
}

public class parkinglot {
    public static void main(String[] args) {
        ParkingLotFacade lot = new ParkingLotFacade(2, 20); // 2 levels, 20 spots each

        Car c1 = new Car("CAR-001");
        Car c2 = new Car("CAR-002");
        Motorcycle m1 = new Motorcycle("MOTO-99");
        Bus b1 = new Bus("BUS-1234");
        Bus b2 = new Bus("BUS-5678"); // Might not fit depending on layout!

        lot.parkVehicle(c1);
        lot.parkVehicle(c2);
        lot.parkVehicle(m1);
        lot.parkVehicle(b1);

        System.out.println();
        lot.print();

        System.out.println();
        lot.parkVehicle(b2); // Let's see if another bus fits!
    }
}
