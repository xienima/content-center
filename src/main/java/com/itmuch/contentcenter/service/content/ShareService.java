package com.itmuch.contentcenter.service.content;


import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {
    private final ShareMapper shareMapper;
    private final RestTemplate restTemplate;


    public ShareDTO findById(Integer id) {
        //获取分享详情
        Share share = this.shareMapper.selectByPrimaryKey(id);
        // 发布人的id
        Integer UserId = share.getUserId();
        // 怎么调用用户微服务的/users/{userid}??

        RestTemplate restTemplate = new RestTemplate();
        // 用HTTP GET方法去请求，并且返回一个对象
        UserDTO userDTO = this.restTemplate.getForObject(
                "http://localhost:8080/users/{id}",
                UserDTO.class, UserId
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