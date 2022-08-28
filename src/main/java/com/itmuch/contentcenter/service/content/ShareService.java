package com.itmuch.contentcenter.service.content;


import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {
    private final ShareMapper shareMapper;
    private final RestTemplate restTemplate;

    private final DiscoveryClient discoveryClient;


    public ShareDTO findById(Integer id) {
        //获取分享详情
        Share share = this.shareMapper.selectByPrimaryKey(id);
        // 发布人的id
        Integer userId = share.getUserId();
        // 怎么调用用户微服务的/users/{userid}??

        RestTemplate restTemplate = new RestTemplate();
        //强调
        //了解stream->Java 8
        // lambda 表达式
        //functional 函数表达式
        // 用户中心所有实例的信息
        List<ServiceInstance> instances = discoveryClient.getInstances("user-center");
        List<String> targetURLS =instances.stream()
                .map(instance->instance.getUri().toString()+"/users/{id}")
                .collect(Collectors.toList());

        int i = ThreadLocalRandom.current().nextInt(targetURLS.size());

        String targetURL =targetURLS.get(i);
        log.info("请求的目标地址为:{}",targetURL);
        // 用HTTP GET方法去请求，并且返回一个对象
        UserDTO userDTO = this.restTemplate.getForObject(
                targetURL,
                UserDTO.class,userId
        );


        // new一个ShareDTO对象
        ShareDTO shareDTO = new ShareDTO();
        //工具类自带的复制属性
        BeanUtils.copyProperties(share,shareDTO);
        //对于shareDTO而言 wxNickname是自己添加不是从share复制过去的，所以呢 需要从userDto中拿
        shareDTO.setWxNickname(userDTO.getWxNickname());

        return shareDTO;

    }
}