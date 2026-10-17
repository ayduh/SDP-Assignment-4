package drone.app;

import drone.facade.AdminFacade;
import drone.facade.DispatchFailedException;
import drone.facade.DispatchRecord;
import drone.facade.InvalidOrderException;
import drone.facade.SimulatedCrashException;
import drone.facade.Stage;
import java.nio.file.Path;
import java.util.Scanner;

/** Console demo. The client only talks to the two facades. */
public class Main {
    public static void main(String[] args) {
        DroneSystem system = new DroneSystem(Path.of(args.length > 0 ? args[0] : "data"));
        AdminFacade admin = system.admin();
        Scanner in = new Scanner(System.in);

        int recovered = admin.recoverAll();
        if (recovered > 0) {
            System.out.println(">> Restart recovery: rolled back " + recovered + " unfinished dispatch(es).");
        }
        System.out.println("Delivery Drone Dispatch. Destinations: CAMPUS, MALL, HOSPITAL, AIRPORT");

        while (true) {
            System.out.println("\n1 New delivery        2 List dispatches     3 Inspect dispatch");
            System.out.println("4 Set wind (km/h)     5 Block landing pad   6 Free blocked pads");
            System.out.println("7 Manual compensation 8 Arm crash (after A/B) 0 Exit");
            System.out.print("> ");
            if (!in.hasNextLine()) {
                return;
            }
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        String dest = ask(in, "Destination: ");
                        double kg = Double.parseDouble(ask(in, "Payload kg: "));
                        DispatchRecord r = system.dispatch().dispatch(dest, kg);
                        System.out.println("OK: " + r.ref() + " delivered, code " + r.getCode());
                    }
                    case "2" -> admin.list().forEach(d -> System.out.println(
                            d.ref() + "  " + d.getDestination() + "  " + d.getPayloadKg() + " kg  " + d.getStatus()));
                    case "3" -> System.out.print(admin.inspect(Integer.parseInt(ask(in, "Dispatch id: "))));
                    case "4" -> admin.setWind(Double.parseDouble(ask(in, "Wind km/h: ")));
                    case "5" -> admin.blockPad(ask(in, "Destination: "));
                    case "6" -> admin.freePads(ask(in, "Destination: "));
                    case "7" -> admin.compensate(Integer.parseInt(ask(in, "Dispatch id: ")));
                    case "8" -> {
                        system.dispatch().crashAfter(Stage.valueOf(ask(in, "Stage (A or B): ").toUpperCase()));
                        System.out.println("Armed. The next delivery will crash the app after that step.");
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Unknown option");
                }
            } catch (DispatchFailedException e) {
                System.out.println("FAILED: " + e.getMessage());
                System.out.println("Final state: " + e.getRecord().getStatus());
            } catch (InvalidOrderException e) {
                System.out.println("Invalid: " + e.getMessage());
            } catch (SimulatedCrashException e) {
                System.out.println("*** " + e.getMessage() + " (process killed). Start the app again to see recovery.");
                System.exit(1);
            } catch (IllegalArgumentException e) {
                System.out.println("Bad input: " + e.getMessage());
            }
        }
    }

    private static String ask(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }
}
