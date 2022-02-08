package adaptation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import domain.URLRequest;

public interface IProbe {
    List<URLRequest> getRequestHistory(String ABComponentName, String variant) throws MalformedURLException, IOException;
}
