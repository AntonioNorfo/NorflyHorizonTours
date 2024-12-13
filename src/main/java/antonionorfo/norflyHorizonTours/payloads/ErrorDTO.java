package antonionorfo.norflyHorizonTours.payloads;

public class ErrorDTO {
    private String message;

    // Costruttore
    public ErrorDTO(String message) {
        this.message = message;
    }

    // Getter
    public String getMessage() {
        return message;
    }

    // Setter
    public void setMessage(String message) {
        this.message = message;
    }
}
