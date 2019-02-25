package org.deeplearning4j.util;

import java.util.Arrays;
import org.nd4j.base.Preconditions;

public class ValidationUtils {

    private ValidationUtils(){

    }

    /**
     * Checks that the values is >= 0.
     *
     * @param data An int
     * @param paramName The param name, for error reporting
     */
    public static void validateNonNegative(int data, String paramName){
        Preconditions.checkArgument(data >= 0,
                "Values for %s must be >= 0, got: %s", paramName, data);
    }

    /**
     * Checks that the values is >= 0.
     *
     * @param data An int
     * @param paramName The param name, for error reporting
     */
    public static void validateNonNegative(double data, String paramName){
        Preconditions.checkArgument(data >= 0,
                "Values for %s must be >= 0, got: %s", paramName, data);
    }

    /**
     * Checks that all values are >= 0.
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     */
    public static void validateNonNegative(int[] data, String paramName){

        if(data == null) {
            return;
        }

        boolean nonnegative = true;

        for(int value : data){
            if(value < 0) {
                nonnegative = false;
            }
        }

        Preconditions.checkArgument(nonnegative,
                "Values for %s must be >= 0, got: %s", paramName, data);
    }

    /**
     * Reformats the input array to a length 1 array and checks that all values are >= 0.
     *
     * If the array is length 1, returns the array
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 1 that represents the input
     */
    public static int[] validate1NonNegative(int[] data, String paramName){
        validateNonNegative(data, paramName);
        return validate1(data, paramName);
    }

    /**
     * Reformats the input array to a length 1 array.
     *
     * If the array is length 1, returns the array
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 1 that represents the input
     */
    public static int[] validate1(int[] data, String paramName){
        if(data == null) {
            return null;
        }

        Preconditions.checkArgument(data.length == 1,
                "Need 1 %s value, got %s values: %s",
                        paramName, data.length, data);

        return data;
    }

    /**
     * Reformats the input array to a length 2 array and checks that all values are >= 0.
     *
     * If the array is length 1, returns [a, a]
     * If the array is length 2, returns the array.
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 2 that represents the input
     */
    public static int[] validate2NonNegative(int[] data, String paramName) {
        validateNonNegative(data, paramName);
        return validate2(data, paramName);
    }

    /**
     * Reformats the input array to a length 2 array.
     *
     * If the array is length 1, returns [a, a]
     * If the array is length 2, returns the array.
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 2 that represents the input
     */
    public static int[] validate2(int[] data, String paramName){
        if(data == null) {
            return null;
        }

        Preconditions.checkArgument(data.length == 1 || data.length == 2,
                "Need either 1 or 2 %s values, got %s values: %s",
                       paramName, data.length, data);

        if(data.length == 1){
            return new int[]{data[0], data[0]};
        } else {
            return data;
        }
    }

    /**
     * Reformats the input array to a 2x2 array and checks that all values are >= 0.
     *
     * If the array is 2x1 ([[a], [b]]), returns [[a, a], [b, b]]
     * If the array is 1x2 ([[a, b]]), returns [[a, b], [a, b]]
     * If the array is 2x2, returns the array
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 2 that represents the input
     */
    public static int[][] validate2x2NonNegative(int[][] data, String paramName){
        for(int[] part : data)
            validateNonNegative(part, paramName);

        return validate2x2(data, paramName);
    }

    /**
     * Reformats the input array to a 2x2 array.
     *
     * If the array is 2x1 ([[a], [b]]), returns [[a, a], [b, b]]
     * If the array is 1x2 ([[a, b]]), returns [[a, b], [a, b]]
     * If the array is 2x2, returns the array
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 2 that represents the input
     */
    public static int[][] validate2x2(int[][] data, String paramName){
        if(data == null) {
            return null;
        }

        Preconditions.checkArgument(
                (data.length == 1 && data[0].length == 2) ||
                        (data.length == 2 &&
                                (data[0].length == 1 || data[0].length == 2) &&
                                (data[1].length == 1 || data[1].length == 2) &&
                                data[0].length == data[1].length
                        ),
                "Value for %s must have shape 2x1, 1x2, or 2x2, got %sx%s shaped array: %s",
                paramName, data.length, data[0].length, data);

        if(data.length == 1) {
            return new int[][]{
                    data[0],
                    data[0]
            };
        } else if(data[0].length == 1){
            return new int[][]{
                    new int[]{data[0][0], data[0][0]},
                    new int[]{data[1][0], data[1][0]}
            };
        } else {
            return data;
        }
    }

    /**
     * Reformats the input array to a length 3 array and checks that all values >= 0.
     *
     * If the array is length 1, returns [a, a, a]
     * If the array is length 3, returns the array.
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 3 that represents the input
     */
    public static int[] validate3NonNegative(int[] data, String paramName){
        validateNonNegative(data, paramName);
        return validate3(data, paramName);
    }

    /**
     * Reformats the input array to a length 3 array.
     *
     * If the array is length 1, returns [a, a, a]
     * If the array is length 3, returns the array.
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 3 that represents the input
     */
    public static int[] validate3(int[] data, String paramName){
        if(data == null) {
            return null;
        }

        Preconditions.checkArgument(data.length == 1 || data.length == 3,
                "Need either 1 or 3 %s values, got %s values: %s",
                paramName, data.length, data);

        if(data.length == 1){
            return new int[]{data[0], data[0], data[0]};
        } else {
            return data;
        }
    }

    /**
     * Reformats the input array to a length 4 array and checks that all values >= 0.
     *
     * If the array is length 1, returns [a, a, a, a]
     * If the array is length 2, return [a, a, b, b]
     * If the array is length 4, returns the array.
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 4 that represents the input
     */
    public static int[] validate4NonNegative(int[] data, String paramName){
        validateNonNegative(data, paramName);
        return validate4(data, paramName);
    }

    /**
     * Reformats the input array to a length 4 array.
     *
     * If the array is length 1, returns [a, a, a, a]
     * If the array is length 2, return [a, a, b, b]
     * If the array is length 4, returns the array.
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 4 that represents the input
     */
    public static int[] validate4(int[] data, String paramName){
        if(data == null) {
            return null;
        }

        Preconditions.checkArgument(data.length == 1 || data.length == 2 || data.length == 4,
                "Need either 1, 2, or 4 %s values, got %s values: %s",
                paramName, data.length, data);

        if(data.length == 1){
            return new int[]{data[0], data[0], data[0], data[0]};
        } else if(data.length == 2){
            return new int[]{data[0], data[0], data[1], data[1]};
        } else {
            return data;
        }
    }

    /**
     * Reformats the input array to a length 6 array and checks that all values >= 0.
     *
     * If the array is length 1, returns [a, a, a, a, a, a]
     * If the array is length 3, return [a, a, b, b, c, c]
     * If the array is length 6, returns the array.
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 6 that represents the input
     */
    public static int[] validate6NonNegative(int[] data, String paramName){
        validateNonNegative(data, paramName);
        return validate6(data, paramName);
    }

    /**
     * Reformats the input array to a length 6 array.
     *
     * If the array is length 1, returns [a, a, a, a, a, a]
     * If the array is length 3, return [a, a, b, b, c, c]
     * If the array is length 6, returns the array.
     *
     * @param data An array
     * @param paramName The param name, for error reporting
     * @return An int array of length 6 that represents the input
     */
    public static int[] validate6(int[] data, String paramName){
        if(data == null) {
            return null;
        }

        Preconditions.checkArgument(data.length == 1 || data.length == 3 || data.length == 6,
                "Need either 1, 3, or 6 %s values, got %s values: %s",
                paramName, data.length, data);

        if(data.length == 1){
            return new int[]{data[0], data[0], data[0], data[0], data[0], data[0]};
        } else if(data.length == 3){
            return new int[]{data[0], data[0], data[1], data[1], data[2], data[2]};
        } else {
            return data;
        }
    }
}