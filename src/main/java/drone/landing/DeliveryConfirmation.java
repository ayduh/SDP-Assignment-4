package drone.landing;

public class DeliveryConfirmation {
    public String codeFor(String orderRef) {
        return "DLV-" + orderRef;
    }
}
