package se.kth.honeytap.scaling.exceptions;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Ashansa Perera
 * @version $Id$
 * @since 1.0
 */
public class ManageGroupException extends HoneyTapException {

    public ManageGroupException(String msg) {
        super(msg);
    }

    public ManageGroupException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
