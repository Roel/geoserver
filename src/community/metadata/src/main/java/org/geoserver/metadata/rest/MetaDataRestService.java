/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.metadata.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.geoserver.metadata.data.service.MetaDataBulkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/metadata")
public class MetaDataRestService {

    @Autowired private MetaDataBulkService bulkService;

    @DeleteMapping
    public void clearAll(
            @RequestParam(required = false, defaultValue = "false") boolean iAmSure,
            @RequestParam(required = false, defaultValue = "false") boolean templatesToo,
            HttpServletResponse response)
            throws IOException {
        if (!iAmSure) {
            response.sendError(400, "You must be sure.");
        } else {
            bulkService.clearAll(templatesToo);
        }
    }

    @GetMapping("fix")
    public String fixAll() {
        bulkService.fixAll();
        return "Success.";
    }

    @PostMapping("nativeToCustom")
    public void nativeToCustom(
            @RequestParam(required = false) String indexes, @RequestBody String csvFile) {
        bulkService.nativeToCustom(convertToList(indexes), csvFile);
    }

    @GetMapping("nativeToCustom")
    public String nativeToCustom(@RequestParam(required = false) String indexes) {
        bulkService.nativeToCustom(convertToList(indexes));
        return "Success.";
    }

    @PostMapping("import")
    public void importAndLink(
            @RequestParam(required = false) String geonetwork, @RequestBody String csvFile) {
        bulkService.importAndLink(geonetwork, csvFile);
    }

    private List<Integer> convertToList(String indexes) {
        if (indexes != null) {
            return Arrays.stream(indexes.split(","))
                    .map(s -> Integer.parseInt((s.trim())))
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }
}
