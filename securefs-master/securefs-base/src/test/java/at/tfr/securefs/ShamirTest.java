package at.tfr.securefs;

import at.tfr.securefs.key.Shamir;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class ShamirTest {

    @Test
    public void testShamir() {
        Configuration config = new Configuration();
        config.init();

        Shamir shamir = new Shamir();
        BigInteger key = shamir.combine(config.getNrOfShares(), config.getThreshold(), config.getModulus(), config.getShares());
        Assert.assertNotNull("failed to combine: " + config.getShares(), key);
    }

}
