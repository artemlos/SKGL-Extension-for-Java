package io.cryptolens.methods;

import io.cryptolens.models.ActivatedMachine;
import io.cryptolens.models.LicenseKey;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * A collection of helper methods that operate on a license key.
 */
public class Helpers {

    /**
     * Returns a unique identifier of the device. Note, root access may be required.
     * Note, this method is not the same as the one used in our .NET client.
     */
    public static String GetMachineCode() {

        return SHA256(getRawDeviceID());
    }

    /**
     * Check if the device is registered with the license key.
     * @returns True if the license is registered with this machine and False otherwise.
     */
    public static boolean IsOnRightMachine(LicenseKey license) {
        return IsOnRightMachine(license, false);
    }

    /**
     * Check if the device is registered with the license key.
     * @param isFloatingLicense If this is a floating license, this parameter has to be set to true.
     *                          You can enable floating licenses by setting @see ActivateModel.FloatingTimeInterval.
     * @returns True if the license is registered with this machine and False otherwise.
     */
    public static boolean IsOnRightMachine(LicenseKey license, boolean isFloatingLicense) {
        return IsOnRightMachine(license, isFloatingLicense, false);
    }

    /**
     * Check if the device is registered with the license key.
     * @param license The license key object
     * @param isFloatingLicense If this is a floating license, this parameter has to be set to true.
     *                          You can enable floating licenses by setting @see ActivateModel.FloatingTimeInterval.
     * @param allowOverdraft If floating licensing is enabled with overdraft, this parameter should be set to true.
     *                       You can enable overdraft by setting ActivateModel.MaxOverdraft" to a value greater than 0.
     *
     * @returns True if the license is registered with this machine and False otherwise.
     */
    public static boolean IsOnRightMachine(LicenseKey license, boolean isFloatingLicense, boolean allowOverdraft) {

        String current_mid = Helpers.GetMachineCode();

        if (license == null || license.ActivatedMachines == null){
            return false;
        }

        if(isFloatingLicense) {
            if(license.ActivatedMachines.size() == 1 &&
                    (license.ActivatedMachines.get(0).Mid.substring(9).equals(current_mid) ||
                            allowOverdraft && license.ActivatedMachines.get(0).Mid.substring(19).equals(current_mid))) {
                return true;
            }
        } else {

            for (ActivatedMachine machine : license.ActivatedMachines) {
                if(machine.Mid.equals(current_mid))
                    return true;
            }
        }

        return false;
    }

    /**
     * Check if the current license has expired.
     * @param licenseKey a license key object.
     * @return True if it has expired and false otherwise.
     */
    public static boolean HasExpired(LicenseKey licenseKey) {

        if(licenseKey == null) {
            return false;
        }

        long unixTime = System.currentTimeMillis() / 1000L;

        if (licenseKey.Expires < unixTime) {
            return true;
        }

        return false;
    }

    /**
     * Check if the current license has not expired.
     * @param licenseKey a license key object.
     * @return True if it has not expired and false otherwise.
     */
    public static boolean HasNotExpired(LicenseKey licenseKey) {

        return !Helpers.HasExpired(licenseKey);
    }

    /**
     * Check if the license has a certain feature enabled (i.e. set to true).
     * @param licenseKey a license key object.
     * @param feature The feature, eg 1 to 8.
     * @return If the feature is set to true, true is returned and false otherwise.
     */
    public static boolean HasFeature(LicenseKey licenseKey, int feature) {

        if(licenseKey == null){
            return false;
        }

        if (feature == 1 && licenseKey.F1)
            return true;
        if (feature == 2 && licenseKey.F2)
            return true;
        if (feature == 3 && licenseKey.F3)
            return true;
        if (feature == 4 && licenseKey.F4)
            return true;
        if (feature == 5 && licenseKey.F5)
            return true;
        if (feature == 6 && licenseKey.F6)
            return true;
        if (feature == 7 && licenseKey.F7)
            return true;
        if (feature == 8 && licenseKey.F8)
            return true;

        return false;
    }

    /**
     * Return the sha256 checksum of a string.
     */
    private static String SHA256(String rawData) {

        StringBuffer hexString = new StringBuffer();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));

            //thanks to https://stackoverflow.com/a/5470268.

            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0"
                            + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }

            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String getRawDeviceID()
    {
        //thanks to https://stackoverflow.com/a/37705082. may require root.
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        ComputerSystem computerSystem = hardwareAbstractionLayer.getComputerSystem();

        String vendor = operatingSystem.getManufacturer();
        String processorSerialNumber = computerSystem.getSerialNumber();
        String processorIdentifier = centralProcessor.getIdentifier();
        int processors = centralProcessor.getLogicalProcessorCount();

        return vendor +
                processorSerialNumber +
                processorIdentifier +
                processors;
    }

}
