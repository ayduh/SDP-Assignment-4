package drone.landing;

/** Pure business logic: builds the confirmation code handed to the recipient. */
public class DeliveryConfirmation {
    public String codeFor(String orderRef) {
        return "DLV-" + orderRef;
    }
}
