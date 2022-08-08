package com.leisurexi.codeeditor.controller;

import com.leisurexi.codeeditor.api.BaseResponse;
import com.leisurexi.codeeditor.dto.ExecuteCodeResponse;
import com.leisurexi.codeeditor.dto.ExecuteResults;
import com.leisurexi.codeeditor.service.IndexService;
import com.leisurexi.codeeditor.testssh.SshBasic;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

/**
 * @description:
 * @since JDK 1.8
 */
@RestController
@Validated
public class IndexController {
    @PostMapping("execute_code")
    public BaseResponse executeCode(@NotBlank(message = "Please input code") String code) throws Exception {
        ExecuteResults executeResults = SshBasic.inputCode(code);
        return new ExecuteCodeResponse(executeResults);
    }

}
