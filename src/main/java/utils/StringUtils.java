package utils;

import beans.TaxiBean;

import java.util.Set;
import java.util.stream.Collectors;

public class StringUtils {

    public static String getSocketAddress(String ipAddress, int portNumber) {
        return ipAddress + ':' + portNumber;
    }

    public static String taxisSetToString(Set<TaxiBean> taxis, int indentationLevel) {
        StringBuilder delimiter = new StringBuilder("\n");
        for (int i = 0; i < indentationLevel; i++)
            delimiter.append('\t');

        return taxis.stream()
                .map(TaxiBean::toString)
                .collect(Collectors.joining(delimiter));
    }
}
