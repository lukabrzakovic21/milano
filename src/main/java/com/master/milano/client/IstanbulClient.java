package com.master.milano.client;

import com.master.milano.common.dto.BasicUserInfoDTO;
import com.master.milano.common.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(value = "istanbul", url = "http://localhost:8081")
public interface IstanbulClient {

    @RequestMapping(method = RequestMethod.GET, value = "/user/{publicId}", produces = "application/json", consumes = "application/json")
    UserDTO getUserByPublicId(@PathVariable String publicId, @RequestParam(name = "include_deactivated", required = false, defaultValue = "false") boolean includeDeactivated);

    @RequestMapping(method = RequestMethod.POST, value = "/user/all", produces = "application/json", consumes = "application/json")
    List<BasicUserInfoDTO> getAllUsersForPublicIds(@RequestBody List<UUID> ids);
}
