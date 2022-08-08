import java.io.*;
import java.lang.*;
import java.math.*;
import java.net.*;
import java.nio.*;
import java.text.*;
import java.time.*;
import java.util.*;
package com.yoc.rxk.saas.admin.service.controller.user;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.google.common.primitives.Ints;
import com.yoc.ptc.common.base.response.RespHandler;
import com.yoc.ptc.common.utils.asserts.AssertUtil;
import com.yoc.ptc.orm.data.encryption.handler.BaseEncDecHandler;
import com.yoc.rxk.saas.admin.service.common.config.LoginLimitService;
import com.yoc.rxk.saas.admin.service.common.constant.CommonConstant;
import com.yoc.rxk.saas.admin.service.common.constant.RedisPrefixConstants;
import com.yoc.rxk.saas.admin.service.common.constant.SysParamConstants;
import com.yoc.rxk.saas.admin.service.common.constant.enums.*;
import com.yoc.rxk.saas.admin.service.common.constant.enums.organ.OrganAppMenuEnum;
import com.yoc.rxk.saas.admin.service.common.constant.enums.saas.SaasUserBillingTypeEnum;
import com.yoc.rxk.saas.admin.service.common.constant.usr.UserInfoUserTypeEnum;
import com.yoc.rxk.saas.admin.service.common.exception.ApplicationException;
import com.yoc.rxk.saas.admin.service.common.exception.DAOException;
import com.yoc.rxk.saas.admin.service.common.utils.auth.AccountAuthUtil;
import com.yoc.rxk.saas.admin.service.common.utils.bean.BeanCopyUtils;
import com.yoc.rxk.saas.admin.service.common.utils.city.CityUtil;
import com.yoc.rxk.saas.admin.service.common.utils.common.*;
import com.yoc.rxk.saas.admin.service.common.utils.excel.EasyExcelUtils;
import com.yoc.rxk.saas.admin.service.common.web.page.PageBean;
import com.yoc.rxk.saas.admin.service.config.RedisTemplateUtil;
import com.yoc.rxk.saas.admin.service.config.redisson.RedissonUtil;
import com.yoc.rxk.saas.admin.service.controller.BaseController;
import com.yoc.rxk.saas.admin.service.entity.api.ApiSource;
import com.yoc.rxk.saas.admin.service.entity.dto.user.UserTransDto;
import com.yoc.rxk.saas.admin.service.entity.flag.UserFlag;
import com.yoc.rxk.saas.admin.service.entity.flowpackage.SaasFlowPackageOrganDO;
import com.yoc.rxk.saas.admin.service.entity.organ.OrganDepartment;
import com.yoc.rxk.saas.admin.service.entity.parameter.ManualRechargeParameter;
import com.yoc.rxk.saas.admin.service.entity.parameter.SaasUserInfoAdminListParameter;
import com.yoc.rxk.saas.admin.service.entity.parameter.UserInfoParameter;
import com.yoc.rxk.saas.admin.service.entity.saas.SaasChannelType;
import com.yoc.rxk.saas.admin.service.entity.saas.SaasOrderFollowCommonWords;
import com.yoc.rxk.saas.admin.service.entity.saas.SaasOrganInfo;
import com.yoc.rxk.saas.admin.service.entity.saas.SaasProductType;
import com.yoc.rxk.saas.admin.service.entity.shop.Shop;
import com.yoc.rxk.saas.admin.service.entity.shop.ShopAutoDistributeConfig;
import com.yoc.rxk.saas.admin.service.entity.statis.StatisSaasGlobal;
import com.yoc.rxk.saas.admin.service.entity.sys.SysMenu;
import com.yoc.rxk.saas.admin.service.entity.sys.SysRole;
import com.yoc.rxk.saas.admin.service.entity.sys.SysUser;
import com.yoc.rxk.saas.admin.service.entity.user.*;
import com.yoc.rxk.saas.admin.service.entity.vo.SaasZyOrganVo;
import com.yoc.rxk.saas.admin.service.entity.vo.SaasZyUserInfoVo;
import com.yoc.rxk.saas.admin.service.mapper.api.ApiSourceMapper;
import com.yoc.rxk.saas.admin.service.mapper.user.UserInfoAuthMapper;
import com.yoc.rxk.saas.admin.service.mapper.user.UserInfoMapper;
import com.yoc.rxk.saas.admin.service.model.dto.flowpackage.SaasOrganClueConfigDTO;
import com.yoc.rxk.saas.admin.service.service.callcenter.YocCallCenterService;
import com.yoc.rxk.saas.admin.service.service.config.OrganTransferRangeConfigServiceImpl;
import com.yoc.rxk.saas.admin.service.service.custom.OrganMenuServiceImpl;
import com.yoc.rxk.saas.admin.service.service.flag.UserFlagServiceImpl;
import com.yoc.rxk.saas.admin.service.service.flowpackage.SaasFlowPackageOrganService;
import com.yoc.rxk.saas.admin.service.service.organ.OrganDepartmentServiceImpl;
import com.yoc.rxk.saas.admin.service.service.organ.OrganSearchTemplateServiceImpl;
import com.yoc.rxk.saas.admin.service.service.organ.OrganTableColumnConfigServiceImpl;
import com.yoc.rxk.saas.admin.service.service.pool.AppExecutePool;
import com.yoc.rxk.saas.admin.service.service.pool.PushExecutePool;
import com.yoc.rxk.saas.admin.service.service.saas.*;
import com.yoc.rxk.saas.admin.service.service.saaszy.SaasZyOrganRelationServiceImpl;
import com.yoc.rxk.saas.admin.service.service.shop.ShopAutoDistributeConfigServiceImpl;
import com.yoc.rxk.saas.admin.service.service.shop.ShopServiceImpl;
import com.yoc.rxk.saas.admin.service.service.statis.StatisSaaSGlobalServiceImpl;
import com.yoc.rxk.saas.admin.service.service.sys.*;
import com.yoc.rxk.saas.admin.service.service.user.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Title: LoanerInfoController
 * Description:
 *
 * @author 姜俊杰
 * @version V1.0
 * @date 2019-05-20
 */
@RestController
@RequestMapping("/web/youka/loaner-info")
public class UserInfoController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(UserInfoController.class);
    @Autowired
    private UserInfoServiceImpl userInfoService;
    @Autowired
    private UserInfoAuthMapper userInfoAuthMapper;
    @Autowired
    private SysUserServiceImpl sysUserService;
    @Autowired
    private UserWalletServiceImpl userWalletService;
    @Autowired
    private SysDesensitizationServiceImpl sysDesensitizationService;
    @Autowired
    private SysParamServiceImpl sysParamService;
    @Autowired
    private ShopServiceImpl shopService;
    @Autowired
    private RedisTemplateUtil redisTemplateUtil;
    @Autowired
    private UserInflowServiceImpl userInflowService;
    @Autowired
    private SysRoleServiceImpl sysRoleService;
    @Autowired
    private SysUserRefRoleServiceImpl sysUserRefRoleService;
    @Autowired
    private UserRegisterApplyRecordImpl userRegisterApplyRecord;
    @Autowired
    private UserFlagServiceImpl userFlagService;
    @Autowired
    private StatisSaaSGlobalServiceImpl statisGlobalService;
    @Autowired
    private AppExecutePool appExecutePool;
    @Autowired
    private OrganDepartmentServiceImpl organDepartmentService;
    @Autowired
    private ShopAutoDistributeConfigServiceImpl shopAutoDistributeConfigService;
    @Autowired
    private ApiSourceMapper apiSourceMapper;
    @Autowired
    private OrganTransferRangeConfigServiceImpl organTransferRangeConfigService;
    @Autowired
    private SaasChannelTypeServiceImpl saasChannelTypeService;
    @Autowired
    private SaasOrganInfoServiceImpl saasOrganInfoService;
    @Autowired
    private UserRecallMsgSendServiceImpl userRecallMsgSendService;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserTransDetailServiceImpl userTransDetailService;
    @Autowired
    private OrganMenuServiceImpl organMenuServiceImpl;
    @Autowired
    private SaasProductTypeServiceImpl saasProductTypeService;
    @Autowired
    private UserInfoConfigServiceImpl userInfoConfigService;
    @Autowired
    private SaasUserRenewRecordServiceImpl saasUserRenewRecordService;
    @Autowired
    private RedissonUtil redissonUtil;
    @Autowired
    private SaasZyOrganRelationServiceImpl saasZyOrganRelationService;
    @Autowired
    private BaseEncDecHandler encDecHandler;
    @Autowired
    private LoginLimitService loginLimitService;
    @Autowired
    private SaasCustomerProgressConfigServiceImpl saasCustomerProgressConfigService;
    @Autowired
    private OrganTableColumnConfigServiceImpl organTableColumnConfigService;
    @Autowired
    private OrganSearchTemplateServiceImpl organSearchTemplateService;
    @Autowired
    private SaasOrderFollowCommonWordsServiceImpl saasOrderFollowCommonWordsService;
    @Autowired
    private OrganUserCacheService organUserCacheService;
    @Autowired
    private UserPhoneChangeRecordServiceImpl userPhoneChangeRecordService;
    @Autowired
    private SaasFlowPackageOrganService saasFlowPackageOrganService;
    @Autowired
    private PushExecutePool pushExecutePool;
    @Autowired
    private YocCallCenterService yocCallCenterService;

    /**
     * 查看号码
     */
    @RequestMapping(value = "/getDetail")
    public RespHandler getDetail(Integer id) {
        try {
            UserInfo userInfo = userInfoService.selectByPrimaryKey(id);
            sysDesensitizationService.addUserClickRecord(getLoginUserId(), id, SysDesensitizationTypeEnum.TYPE_100.getCode());
            return RespHandler.success(userInfo);
        } catch (Exception e) {
            logger.error("获取失败！", e);
            return RespHandler.error("获取失败！");
        }
    }


    /**
     * Title: selectList
     * Description:
     * <p>  saas  管理员
     * * @param params
     */
    @RequestMapping(value = "/selectOrgAdminList", method = RequestMethod.GET)
    public RespHandler selectOrgAdminList(SaasUserInfoAdminListParameter parameter) {
        try {
            PageHelper.startPage(getPageNum(), getPageSize());
            List<UserInfoDetailVo> userInfoList = userInfoService.selectOrgUserList(parameter);
            for (UserInfoDetailVo item : userInfoList) {
                item.setPhone(PhoneUtil.mobileEncrypt4(item.getPhone()));
            }
            PageBean<UserInfoDetailVo> pageBean = new PageBean<>(userInfoList);
            pageBean.setMeta(new PageBean<>(userInfoList).getMeta());
            pageBean.setDatas(userInfoList);
            return RespHandler.success(pageBean);
        } catch (Exception e) {
            logger.error("获取列表失败！", e);
            return RespHandler.error("获取列表失败！");
        }
    }

    /**
     * Title: selectList
     * Description:
     * <p>
     * * @param params
     */
    @RequestMapping(value = "/selectList", method = RequestMethod.GET)
    public RespHandler selectList(UserInfoParameter entity) {
        try {
            //用户标签字段
            if (entity.getUserFlagId() != null) {
                UserFlag userFlag = userFlagService.selectByPrimaryKey(entity.getUserFlagId());
                entity.setFlagSql(userFlag.getFlagSql());
            }
            PageHelper.startPage(getPageNum(), getPageSize());
            List<UserInfoVo> userInfos = userInfoService.selectList(entity);
            for (UserInfoVo item : userInfos) {
                UserInfoAuth userInfoAuth = userInfoAuthMapper.getRecordSuccessByUserId(item.getId());
                if (null != userInfoAuth) {
                    item.setBasicInfoAuthTime(userInfoAuth.getBasicInfoAuthTime());
                    item.setCity(userInfoAuth.getCity());
                    item.setOrganName(userInfoAuth.getOrganizationName());
                }
                item.setShowName(item.getRealName());
                item.setShowPhone(item.getPhone());
                item.setPhone(PhoneUtil.mobileEncrypt4(item.getPhone()));
                item.setRealName(TuoMinUtil.nameEncrypt(item.getRealName()));
                item.setLoginName(TuoMinUtil.nameEncrypt(item.getLoginName()));

            }
            PageBean<UserInfoVo> pageBean = new PageBean<>(userInfos);
            pageBean.setMeta(new PageBean<>(userInfos).getMeta());
            pageBean.setDatas(userInfos);
            return RespHandler.success(pageBean);
        } catch (Exception e) {
            logger.error("获取列表失败！", e);
            return RespHandler.error("获取列表失败！");
        }
    }


    /**
     * Title: selectList
     * Description:
     * <p>  saas 成员
     * * @param params
     */
    @RequestMapping(value = "/selectOrgMembersList", method = RequestMethod.GET)
    public RespHandler selectOrgMembersList(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "parentId", required = false) Integer parentId,
            @RequestParam(value = "loginName", required = false) String loginName,
            @RequestParam(value = "realName", required = false) String realName,
            @RequestParam(value = "organName", required = false) String organName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "useStatus", required = false) Integer useStatus,
            @RequestParam(value = "acceptDistribute", required = false) Integer acceptDistribute,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        try {
            Map<String, Object> param = new HashMap<>(16);
            param.put("id", id);
            param.put("parentId", parentId);
            param.put("loginName", loginName);
            param.put("realName", realName);
            param.put("organName", organName);
            param.put("phone", phone);
            param.put("useStatus", useStatus);
            param.put("acceptDistribute", acceptDistribute);
            param.put("beginTime", beginTime);
            param.put("endTime", endTime);
            PageHelper.startPage(getPageNum(), getPageSize());
            List<Map<String, Object>> list = userInfoService.selectOrgMembersList(param);
            for (Map<String, Object> map : list) {
                map.put("loginName", TuoMinUtil.nameEncrypt(String.valueOf(map.get("loginName"))));
                map.put("realName", TuoMinUtil.nameEncrypt(String.valueOf(map.get("realName"))));
                map.put("phone", PhoneUtil.mobileEncrypt4(String.valueOf(map.get("phone"))));
            }
            PageBean<Map<String, Object>> pageBean = new PageBean<>(list);
            pageBean.setMeta(new PageBean<>(list).getMeta());
            pageBean.setDatas(list);
            return RespHandler.success(pageBean);
        } catch (Exception e) {
            logger.error("获取列表失败！", e);
            return RespHandler.error("获取列表失败！");
        }
    }

    /**
     * Title: selectList - SaaS管理员
     * Description:
     * <p>
     * * @param params
     */
    @RequestMapping(value = "/selectOrgMembersListForSaaS", method = RequestMethod.GET)
    public RespHandler selectOrgMembersListForSaaS(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "parentId", required = false) Integer parentId,
            @RequestParam(value = "loginName", required = false) String loginName,
            @RequestParam(value = "realName", required = false) String realName,
            @RequestParam(value = "organName", required = false) String organName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "useStatus", required = false) Integer useStatus,
            @RequestParam(value = "acceptDistribute", required = false) Integer acceptDistribute,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        try {
            Map<String, Object> param = new HashMap<String, Object>(16);
            param.put("id", id);
            param.put("parentId", parentId);
            param.put("loginName", loginName);
            param.put("realName", realName);
            param.put("organName", organName);
            param.put("phone", phone);
            param.put("useStatus", useStatus);
            param.put("acceptDistribute", acceptDistribute);
            param.put("beginTime", beginTime);
            param.put("endTime", endTime);
            PageHelper.startPage(getPageNum(), getPageSize());
            List<Map<String, Object>> list = userInfoService.selectOrgMembersListForSaas(param);
            for (Map<String, Object> map : list) {
                map.put("loginName", TuoMinUtil.nameEncrypt(String.valueOf(map.get("loginName"))));
                map.put("realName", TuoMinUtil.nameEncrypt(String.valueOf(map.get("realName"))));
                map.put("phone", PhoneUtil.mobileEncrypt4(String.valueOf(map.get("phone"))));
            }
            PageBean<Map<String, Object>> pageBean = new PageBean<>(list);
            pageBean.setMeta(new PageBean<>(list).getMeta());
            pageBean.setDatas(list);
            return RespHandler.success(pageBean);
        } catch (Exception e) {
            logger.error("获取列表失败！", e);
            return RespHandler.error("获取列表失败！");
        }
    }

    @RequestMapping(value = "/selectDetail", method = RequestMethod.GET)
    public RespHandler selectDetail(
            @RequestParam(value = "id") Integer id) {
        try {
            UserInfo userInfo = userInfoService.getInfoById(id);
            UserInfoDetailVo item = new UserInfoDetailVo();
            BeanUtils.copyProperties(userInfo, item);
            //登录名字可能是电话，如果是电话，则脱敏处理
            item.setPhone(PhoneUtil.mobileEncrypt4(item.getPhone()));
            item.setLoginName(TuoMinUtil.convertEncrypt(item.getLoginName()));
            item.setIdcard(IdCardUtil.desensitizeIdCard(item.getIdcard()));
            item.setRealName(TuoMinUtil.nameEncrypt(item.getRealName()));
            List<UserPhoneChangeRecord> userPhoneChangeRecords = userPhoneChangeRecordService.selectHistoryByUser(id);
            for (UserPhoneChangeRecord record : userPhoneChangeRecords) {
                record.setPhoneNew(PhoneUtil.mobileEncrypt4(record.getPhoneNew()));
                record.setPhoneOld(PhoneUtil.mobileEncrypt4(record.getPhoneOld()));
                record.setUserName(TuoMinUtil.nameEncrypt(record.getUserName()));
                record.setUpdateUserName(TuoMinUtil.nameEncrypt(record.getUpdateUserName()));
            }
            item.setUserPhoneChangeRecords(userPhoneChangeRecordService.selectHistoryByUser(id));
            Integer superId = userInfo.getParentId() == 0 ? id : userInfo.getParentId();
            UserWallet userWallet = userWalletService.selectByUserId(superId);
            item.setCoin(userWallet.getCoin());
            item.setVoucher(userWallet.getVoucher());

            // 融享客展业新增字段
            SaasOrganInfo saasOrganInfo = saasOrganInfoService.getBySuperId(superId);
            if (null != saasOrganInfo) {
                if (!StringUtil.isEmpty(saasOrganInfo.getCity())) {
                    String city = Arrays.asList(saasOrganInfo.getCity().split(",")).get(1);
                    // 所在城市
                    if ("市辖区".equals(city)) {
                        item.setBelongCity(saasOrganInfo.getProvince());
                    } else {
                        item.setBelongCity(saasOrganInfo.getCity());
                    }
                }
                // 公司简称
                item.setShortName(saasOrganInfo.getShortName());
                // 机构地址
                item.setOrganAddress(saasOrganInfo.getAgencyAddress());
                // 机构类型
                item.setCompanyType(saasOrganInfo.getCompanyType());
            }
            return RespHandler.success(item);
        } catch (Exception e) {
            logger.error("获取列表失败！", e);
            return RespHandler.error("获取列表失败！");
        }
    }

    /**
     * 获取SaaS用户充值页面数据   余额 ，系统用户手机号
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/getUserCoinById", method = RequestMethod.GET)
    public RespHandler getUserCoinById(@RequestParam("id") Integer id) {
        try {
            Map<Object, Object> map = new HashMap<>(4);
            map.put("userCoin", userWalletService.selectByUserId(id).getCoin());
            map.put("sysUserPhone", sysUserService.getUserByUserId(Integer.valueOf(getSysUserId())).getPhone());
            return RespHandler.success(map);
        } catch (Exception e) {
            logger.error("获取数据失败", e);
            return RespHandler.error("获取数据失败");
        }
    }

    /**
     * saas用户后台充值
     *
     * @param userTransDto 变更类型 1-充值；2-减扣,3-退单,4-退保证金
     * @return
     */
    @RequestMapping(value = "/offLineRecharge", method = RequestMethod.POST)
    public RespHandler<Void> offLineRecharge(UserTransDto userTransDto) throws Exception {
        AssertUtil.isTrue(ObjectUtil.isNotEmpty(userTransDto.getChargeType()), "变更方式不能为空!");
        AssertUtil.isTrue(ObjectUtil.isNotEmpty(userTransDto.getChargeMoney()), "金额不能为空!");
        int enable = Integer.parseInt(sysParamService.getSysPramByCode(SysParamConstants.IS_ENABLE_CODE_LOGIN).getValue());
        //是否开启验证码校验 0-否，1-是
        if (enable == 1) {
            String key = RedisPrefixConstants.ADMIN_PHONE_CODE + encDecHandler.encrypt(userTransDto.getPhone()) + ":userType:" + UserInfoUserTypeEnum.USER_TYPE_0.getIntCode();
            if (!redisTemplateUtil.exists(key) ||
                    !redisTemplateUtil.get(key).equalsIgnoreCase(userTransDto.getCode())) {
                redisTemplateUtil.del(key);
                return RespHandler.error("验证码错误！");
            }
            redisTemplateUtil.del(key);
        }
        userInflowService.offLineRecharge(userTransDto, Integer.valueOf(getSysUserId()));
        return RespHandler.success("操作成功");
    }

    /**
     * Title: insert
     * Description:
     * <p>
     * * @param record
     */
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public RespHandler insert(UserInfo record) {
        try {
            String city = CityUtil.formatCityName(record.getCity());
            record.setCity(city);
            record.setCreateUserName(sysUserService.getUserByUserId(Integer.valueOf(getSysUserId())).getLoginName());
            Date date = new Date();
            record.setActivateTime(date);
            record.setCreateTime(date);
            record.setActivateApp("1");
            record.setParentId(0);
            List<UserInfo> userInfos = userInfoService.getUserInfoByPhone(record.getPhone(), record.getUserType());
            if (userInfos.size() > 0) {
                return RespHandler.error("手机号已存在！");
            }
            List<UserInfo> userInfos1 = userInfoService.getListByLoginName(record.getLoginName(), record.getUserType());
            if (userInfos1.size() > 0) {
                return RespHandler.error("登录账号已存在！");
            }
            // 如果是saas本地用户，创建用户授权码并且校验
            if (record.getUserType() == Integer.parseInt(UserInfoUserTypeEnum.USER_TYPE_300.getCode())) {
                String userLicense;
                do {
                    userLicense = UUID.randomUUID().toString().toUpperCase();
                } while (this.userInfoService.checkUserLicenseExists(userLicense));

                // 生成 账号限制授权码，系统使用期限授权
                String accountAstrictCode = AccountAuthUtil.generateAccountAstrictCode(record.getPhone(), record.getAccountNumber());
                String accountUseDeadCode = AccountAuthUtil.generateAccountUseDeadCode(record.getPhone(), Calendar.YEAR, 1);
                record.setUserLicense(userLicense + "," + accountAstrictCode + "," + accountUseDeadCode);
            }
            if (userInfoService.insertSelective(record)) {
                UserWallet userWallet = new UserWallet();
                userWallet.setUserId(record.getId());
                userWallet.setScore(0);
                userWallet.setCoin(BigDecimal.ZERO);
                userWallet.setVoucher(BigDecimal.ZERO);
                userWallet.setDrawMoney(BigDecimal.ZERO);
                userWallet.setDongjieMoney(BigDecimal.ZERO);
                userWalletService.saveEntity(userWallet);
                //SaaS管理员，初始化shop数据
                if (record.getUserType() == Integer.parseInt(UserInfoUserTypeEnum.USER_TYPE_200.getCode())) {
                    //saas2.6.3临时需求 扣费方式配置处理
                    castUserBillingTypeConfig(record);
                    Shop shop = new Shop();
                    shop.setUserId(record.getId());
                    shop.setUserName(record.getRealName());
                    shop.setUserPhone(record.getPhone());
                    shop.setUserCity(city);
                    shop.setSwitchStatus(SwitchEnum.CLOSE.getIntCode());
                    shop.setCreateTime(new Date());
                    shop.setUserType(Integer.parseInt(UserInfoUserTypeEnum.USER_TYPE_200.getCode()));
                    shopService.save(shop);
                    //初始化一个机构4个角色(超管/信贷员/客户经理/团队主管)
                    //超管
                    SysRole sysRole1 = new SysRole();
                    sysRole1.setSystemType(SysMenu.SystemType.RXK_ORGAN.getIntCode());
                    sysRole1.setName("超级管理员");
                    sysRole1.setStatus(SysRole.RoleStatusEnum.STATUS_USED.getIntCode());
                    sysRole1.setRoleFlag(SysRole.RoleFlagEnum.ROLE_FLAG_100.getIntCode());
                    sysRoleService.addRole(sysRole1);
                    record.setCurrentRoleId(sysRole1.getId());
                    userInfoService.updateByPrimaryKeySelective(record);
                    //信贷员
                    SysRole sysRole2 = new SysRole();
                    sysRole2.setSystemType(SysMenu.SystemType.RXK_ORGAN.getIntCode());
                    sysRole2.setParentId(sysRole1.getId());
                    sysRole2.setName("信贷员");
                    sysRole2.setStatus(SysRole.RoleStatusEnum.STATUS_USED.getIntCode());
                    sysRole2.setRoleFlag(SysRole.RoleFlagEnum.ROLE_FLAG_300.getIntCode());
                    sysRoleService.addRole(sysRole2);
                    //客户经理
                    SysRole sysRole3 = new SysRole();
                    sysRole3.setSystemType(SysMenu.SystemType.RXK_ORGAN.getIntCode());
                    sysRole3.setParentId(sysRole1.getId());
                    sysRole3.setName("客户经理");
                    sysRole3.setStatus(SysRole.RoleStatusEnum.STATUS_USED.getIntCode());
                    sysRole3.setRoleFlag(SysRole.RoleFlagEnum.ROLE_FLAG_300.getIntCode());
                    sysRoleService.addRole(sysRole3);
                    //团队主管
                    SysRole sysRole4 = new SysRole();
                    sysRole4.setSystemType(SysMenu.SystemType.RXK_ORGAN.getIntCode());
                    sysRole4.setParentId(sysRole1.getId());
                    sysRole4.setName("团队主管");
                    sysRole4.setStatus(SysRole.RoleStatusEnum.STATUS_USED.getIntCode());
                    sysRole4.setRoleFlag(SysRole.RoleFlagEnum.ROLE_FLAG_300.getIntCode());
                    sysRoleService.addRole(sysRole4);
                    //保存角色和用户的中间表（给主账号 加上 超管角色）
                    sysUserRefRoleService.saveUserRole(sysRole1.getId(), record.getId(), SysMenu.SystemType.RXK_ORGAN.getIntCode());


                    // 初始化主账号部门信息
                    OrganDepartment organDepartment = new OrganDepartment();
                    organDepartment.setUserId(record.getId());
                    organDepartment.setLevel(1);
                    organDepartment.setName(record.getOrganName());
                    organDepartment.setParentId(0);
                    organDepartment.setAdminId(record.getId());
                    organDepartment.setAdminName(record.getRealName());
                    int addNo = this.organDepartmentService.insertSelective(organDepartment);
                    if (addNo != 0) {
                        UserInfo ui = new UserInfo();
                        ui.setId(record.getId());
                        ui.setOrganDepId(organDepartment.getId());
                        this.userInfoService.updateByPrimaryKeySelective(ui);
                    }

                    // 初始化机构主账号信息及预设角色
                    Map<String, Integer> roleIdMap = new HashMap<>(8);
                    roleIdMap.put("superAdminRoleId", record.getCurrentRoleId());
                    roleIdMap.put("customerManagerRoleId", sysRole3.getId());
                    roleIdMap.put("teamLeaderRoleId", sysRole4.getId());
                    initMainInfo(record, roleIdMap);
                    //初始化客户阶段配置
                    saasCustomerProgressConfigService.initData(record.getId());
                    // 如果是saas用户，推送钱包用户信息
                    CompletableFuture.runAsync(() -> this.pushExecutePool.pushOrganInfo2Wallet(record));
                } else if (record.getUserType().compareTo(UserInfoTypeEnum.CLIENT.getType()) == 0) {
                    //初始化机构信息 20211203 (本地化cps需求)  saas_organ_info
                    SaasOrganInfo saasOrganInfo = new SaasOrganInfo();
                    saasOrganInfo.setBelongsAgency(record.getOrganName());
                    saasOrganInfo.setShortName(record.getShortName());
                    saasOrganInfo.setUserId(record.getId());
                    saasOrganInfoService.saveOrUpdateSaasOrganInfo(saasOrganInfo);
                    // 如果是saas本地用户，推送钱包用户信息
                    CompletableFuture.runAsync(() -> this.pushExecutePool.pushAddSaasClientUserInfo(record));
                }

                // 如果有官网申请用户ID传入
                if (null != record.getApplyId()) {
                    UserRegisterApplyRecord userRegisterApplyRecord = this.userRegisterApplyRecord.selectById(record.getApplyId());
                    if (null != userRegisterApplyRecord &&
                            userRegisterApplyRecord.getStatus().compareTo(UserRegisterApplyRecord.UserRegisterApplyRecordStatusEnum.NOT_FOLLOW.getStatus()) == 0) {
                        userRegisterApplyRecord.setStatus(UserRegisterApplyRecord.UserRegisterApplyRecordStatusEnum.HAVE_OPENED.getStatus());
                        this.userRegisterApplyRecord.update(userRegisterApplyRecord);

                        // saas用户每日统计-当日开通账户总数 + 1
                        StatisSaasGlobal statisSaasGlobal = new StatisSaasGlobal();
                        statisSaasGlobal.setTotalOpenAccountCount(1);
                        this.statisGlobalService.addToDayCount(statisSaasGlobal);
                    }
                }
            }

            // 融享客展业机构关联入库
            if (null != record.getZyOrganId() && !StringUtil.isEmpty(record.getZyOrganName())) {
                saasZyOrganRelationService.insertOrUpdate(record);
            }
            boolean bool = (record.getUserType().compareTo(UserInfoUserTypeEnum.USER_TYPE_200.getIntCode()) == 0 || record.getUserType().compareTo(UserInfoUserTypeEnum.USER_TYPE_300.getIntCode()) == 0);
            if (bool) {
                SaasProductType saasProductType = saasProductTypeService.selectByPrimaryKey(record.getSaasProductTypeId());
                appExecutePool.pushToQbProductType(record.getPhone(), record.getUserType(), saasProductType.getName());
                //呼叫中心同步机构
                yocCallCenterService.syncCallCenterOrganName(record.getOrganName(), record.getId(), true);
            }
            return RespHandler.success("新增成功!");
        } catch (Exception e) {
            logger.error("新增失败！", e);
            return RespHandler.error("新增失败！");
        }
    }

    /**
     * @param record
     * @Description saas机构扣费方式配置处理
     * @date 2021-07-09
     **/
    private void castUserBillingTypeConfig(UserInfo record) {
        if (record.getBillingType() != null) {
            SaasUserBillingTypeEnum instance = SaasUserBillingTypeEnum.getInstance(record.getBillingType());
            if (instance != null) {
                String configJsonStr = record.getConfigJsonStr();
                switch (instance) {
                    //月付版本
                    case BILLING_TYPE_30:
                    case BILLING_TYPE_40:
                        if (StringUtil.isNull(configJsonStr)) {
                            ApplicationException.throwApplicationException("企业计费方式配置参数必传");
                        }
                        JSONObject jsonObject = JSONUtil.parseObj(configJsonStr);
                        if (!jsonObject.containsKey("payCoin")) {
                            ApplicationException.throwApplicationException("企业扣费金币参数必传");
                        }
                        if (!jsonObject.containsKey("delayDay")) {
                            ApplicationException.throwApplicationException("企业到期延时关闭参数必传");
                        }
                        UserInfoConfig userInfoConfig = userInfoConfigService.getByUserId(record.getId(), UserInfoConfigEnum.TypeEnum.TYPE_140.getCode());
                        if (userInfoConfig != null) {
                            userInfoConfig.setConfigJsonStr(configJsonStr);
                            userInfoConfigService.update(userInfoConfig);
                        } else {
                            userInfoConfig = new UserInfoConfig();
                            userInfoConfig.setUserId(record.getId());
                            userInfoConfig.setType(UserInfoConfigEnum.TypeEnum.TYPE_140.getCode());
                            userInfoConfig.setConfigJsonStr(configJsonStr);
                            try {
                                userInfoConfigService.insertSelective(userInfoConfig);
                            } catch (Exception e) {
                                logger.error("企业计费方式配置参数保存异常", e);
                                ApplicationException.throwApplicationException("企业计费方式配置参数保存异常");
                            }
                        }
                        break;
                    case BILLING_TYPE_10:
                    case BILLING_TYPE_20:
                    default:
                        userInfoConfigService.deleteConfigByUserIdAndType(record.getId(), UserInfoConfigEnum.TypeEnum.TYPE_140.getCode());
                        break;
                }
            }
        }
    }


    private void initMainInfo(UserInfo userInfo, Map<String, Integer> roleIdMap) throws Exception {
        Date date = new Date();
        //初始化自动派发参数
        ShopAutoDistributeConfig distributeConfig = new ShopAutoDistributeConfig();
        distributeConfig.setUserId(userInfo.getId());
        shopAutoDistributeConfigService.add(distributeConfig);
        //初始化4个默认渠道配置
        ApiSource apiSource1 = new ApiSource();
        apiSource1.setUserId(userInfo.getId());
        apiSource1.setCreateTime(date);
        apiSource1.setSourceCode(ApiSourceEnum.SCW.getCode());
        apiSource1.setSourceName(ApiSourceEnum.SCW.getDesc());
        apiSourceMapper.insertSelective(apiSource1);
        ApiSource apiSource5 = new ApiSource();
        apiSource5.setUserId(userInfo.getId());
        apiSource5.setCreateTime(date);
        apiSource5.setSourceCode(ApiSourceEnum.XY.getCode());
        apiSource5.setSourceName(ApiSourceEnum.XY.getDesc());
        apiSourceMapper.insertSelective(apiSource5);
        ApiSource apiSource90 = new ApiSource();
        apiSource90.setUserId(userInfo.getId());
        apiSource90.setCreateTime(date);
        apiSource90.setSourceCode(ApiSourceEnum.XXY.getCode());
        apiSource90.setSourceName(ApiSourceEnum.XXY.getDesc());
        apiSourceMapper.insertSelective(apiSource90);
        ApiSource apiSource2 = new ApiSource();
        apiSource2.setUserId(userInfo.getId());
        apiSource2.setCreateTime(date);
        apiSource2.setSourceCode(ApiSourceEnum.YXQB.getCode());
        apiSource2.setSourceName(ApiSourceEnum.YXQB.getDesc());
        apiSourceMapper.insertSelective(apiSource2);
        ApiSource apiSource3 = new ApiSource();
        apiSource3.setUserId(userInfo.getId());
        apiSource3.setCreateTime(date);
        apiSource3.setSourceCode(ApiSourceEnum.HBFX.getCode());
        apiSource3.setSourceName(ApiSourceEnum.HBFX.getDesc());
        apiSourceMapper.insertSelective(apiSource3);
        ApiSource apiSource4 = new ApiSource();
        apiSource4.setUserId(userInfo.getId());
        apiSource4.setCreateTime(date);
        apiSource4.setSourceCode(ApiSourceEnum.SDTJ.getCode());
        apiSource4.setSourceName(ApiSourceEnum.SDTJ.getDesc());
        apiSource4.setType(1);
        apiSourceMapper.insertSelective(apiSource4);
        apiSource4 = new ApiSource();
        apiSource4.setUserId(userInfo.getId());
        apiSource4.setCreateTime(date);
        apiSource4.setSourceCode(ApiSourceEnum.WGW.getCode());
        apiSource4.setSourceName(ApiSourceEnum.WGW.getDesc());
        apiSourceMapper.insertSelective(apiSource4);
        ApiSource apiSourceXxl = new ApiSource();
        apiSourceXxl.setUserId(userInfo.getId());
        apiSourceXxl.setCreateTime(date);
        apiSourceXxl.setSourceCode(ApiSourceEnum.XXL.getCode());
        apiSourceXxl.setSourceName(ApiSourceEnum.XXL.getDesc());
        apiSourceMapper.insertSelective(apiSourceXxl);
        ApiSource apiSourceXxyw = new ApiSource();
        apiSourceXxyw.setUserId(userInfo.getId());
        apiSourceXxyw.setCreateTime(date);
        apiSourceXxyw.setSourceCode(ApiSourceEnum.XXYW.getCode());
        apiSourceXxyw.setSourceName(ApiSourceEnum.XXYW.getDesc());
        apiSourceMapper.insertSelective(apiSourceXxyw);
        ApiSource apiSourceYljf = new ApiSource();
        apiSourceYljf.setUserId(userInfo.getId());
        apiSourceYljf.setCreateTime(date);
        apiSourceYljf.setSourceCode(ApiSourceEnum.YLJF.getCode());
        apiSourceYljf.setSourceName(ApiSourceEnum.YLJF.getDesc());
        apiSourceMapper.insertSelective(apiSourceYljf);
        ApiSource apiSourceByd = new ApiSource();
        apiSourceByd.setUserId(userInfo.getId());
        apiSourceByd.setCreateTime(date);
        apiSourceByd.setSourceCode(ApiSourceEnum.BYD.getCode());
        apiSourceByd.setSourceName(ApiSourceEnum.BYD.getDesc());
        apiSourceMapper.insertSelective(apiSourceByd);
        ApiSource apiSourceYljfXxl = new ApiSource();
        apiSourceYljfXxl.setUserId(userInfo.getId());
        apiSourceYljfXxl.setCreateTime(date);
        apiSourceYljfXxl.setSourceCode(ApiSourceEnum.YLJF_XXLA.getCode());
        apiSourceYljfXxl.setSourceName(ApiSourceEnum.YLJF_XXLA.getDesc());
        apiSourceMapper.insertSelective(apiSourceYljfXxl);
        ApiSource apiSourceYljfXxlB = new ApiSource();
        apiSourceYljfXxlB.setUserId(userInfo.getId());
        apiSourceYljfXxlB.setCreateTime(date);
        apiSourceYljfXxlB.setSourceCode(ApiSourceEnum.YLJF_XXLB.getCode());
        apiSourceYljfXxlB.setSourceName(ApiSourceEnum.YLJF_XXLB.getDesc());
        apiSourceMapper.insertSelective(apiSourceYljfXxlB);
        //初始化  table_column_config...
        this.organTableColumnConfigService.init(userInfo.getId());
        // 初始化 organ_search_template_config
        this.organSearchTemplateService.init(userInfo.getId());
        //初始化  organ_transfer_range_config
        this.organTransferRangeConfigService.init(userInfo.getId());
        //初始化渠道类型
        this.initsaasChannerlType(userInfo.getId());
        //初始化常用语
        this.initSaasOrderFollowCommonWords(userInfo.getId());
        //初始化机构信息  saas_organ_info
        SaasOrganInfo saasOrganInfo = new SaasOrganInfo();
        saasOrganInfo.setBelongsAgency(userInfo.getOrganName());
        saasOrganInfo.setShortName(userInfo.getShortName());
        saasOrganInfo.setUserId(userInfo.getId());
        if (null != userInfo.getMaxOrderCount()) {
            saasOrganInfo.setMaxOrderCount(userInfo.getMaxOrderCount());
        }
        saasOrganInfoService.saveOrUpdateSaasOrganInfo(saasOrganInfo);
        //维护机构菜单数据
        CompletableFuture.runAsync(() -> {
            //初始化机构菜单
            organMenuServiceImpl.initOrganMenu(userInfo.getId());
            //维护机构菜单
            SaasProductType newRecord = saasProductTypeService.selectByPrimaryKey(userInfo.getSaasProductTypeId());
            List<String> productFunctions = StringUtil.toList(newRecord.getApply());
            organMenuServiceImpl.processUserAppMenu(userInfo.getId(), productFunctions, OrganAppMenuEnum.OrganAppMenuProcessTypeEnum.ADD.getCode());
            //初始化机构预设角色和菜单的关联关系
            organMenuServiceImpl.initPreinstallRoleMenus(userInfo.getId(), roleIdMap);
            //初始化
        }).exceptionally(e -> {
            logger.error("初始化机构菜单异常，错误信息", e);
            return null;
        });
    }

    private void initsaasChannerlType(Integer userId) throws Exception {
        String[] names = {"信息流渠道", "短信渠道", "贷超渠道", "微信渠道", "百度渠道", "论坛渠道", "其他"};
        for (String name : names) {
            SaasChannelType saasChannelType = new SaasChannelType();
            saasChannelType.setName(name);
            saasChannelType.setUserId(userId);
            saasChannelType.setCanDelete(0);
            saasChannelType.setCreateTime(new Date());
            saasChannelTypeService.insertSelective(saasChannelType);
        }
    }

    /**
     * @param userId
     * @return void
     * @Description 初始化常用语
     * @author lxd
     * @date 2021-01-19
     **/
    private void initSaasOrderFollowCommonWords(Integer userId) {
        String[] contents = {"电话无人接听", "客户无意向", "客户贷款意向适中，后续继续跟进", "客户贷款意向较强，成交几率较大"};
        List<SaasOrderFollowCommonWords> list = new ArrayList<>();
        for (int i = 0; i < contents.length; i++) {
            String content = contents[i];
            SaasOrderFollowCommonWords item = new SaasOrderFollowCommonWords();
            item.setSort(i + 1);
            item.setCreateTime(new Date());
            item.setType(10);
            item.setUserId(userId);
            item.setContent(content);
            SaasOrderFollowCommonWords enterItem = new SaasOrderFollowCommonWords();
            BeanUtils.copyProperties(item, enterItem);
            list.add(item);
            enterItem.setType(20);
            list.add(enterItem);
        }
        saasOrderFollowCommonWordsService.batchInsert(list);
    }

    /**
     * Title:
     * Description:
     * <p>
     * * @param record
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public RespHandler update(UserInfo record) {
        try {
            UserInfo old = userInfoService.selectByPrimaryKey(record.getId());
            if (old == null) {
                return RespHandler.error("用户信息异常！");
            }
            //禁止修改用户类型
            record.setUserType(old.getUserType());
            //如果电话是脱敏数据，则无需修改
            String phone = record.getPhone();
            if (phone.contains(CommonConstant.ASTERISK)) {
                record.setPhone(old.getPhone());
                phone = old.getPhone();
            }

            //如果变更状态为不可用，Saas展业用户权限全部关闭
            if (record.getUseStatus() == 0) {
                userInfoMapper.closeOrgSaasZyAuth(record.getId());
            }
            String oldRealName = old.getRealName();
            // 判断登录账号是否存在
            int users = userInfoService.getListByLoginNameAndIdNot(record.getLoginName(),
                    String.valueOf(old.getUserType()), record.getId());
            if (users > 0) {
                return RespHandler.error("账号已存在！");
            }
            //判断手机是否存在
            int users1 = userInfoService.getUserInfoByPhoneAndIdNot(phone,
                    String.valueOf(old.getUserType()), record.getId());

            boolean pushFlag = false;
            SaasOrganInfo saasOrganInfo = saasOrganInfoService.getBySuperId(record.getId());
            SaasZyUserInfoVo zyUserInfoVo = SaasZyUserInfoVo.builder().oldPhone(old.getPhone()).saleUserName(record.getSaleUserName())
                    .useStatus(record.getUseStatus()).serviceStatus(old.getServiceStatus()).build();
            //如果手机号码
            if (!phone.equals(old.getPhone())) {
                // 检查融享客展业用户手机号是否存在
                ApplicationException.throwApplicationException(users1 > 0 || userInfoService.existZy(phone), "该手机号已存在！");
                zyUserInfoVo.setPhone(phone);
                // 自动同步融享客展业app用户
                userInfoService.autoSyncRxkZyUser(zyUserInfoVo);
                userInfoService.updatePhone(phone, record.getId(), getLoginUserId());
                record.setPhone(phone);
                pushFlag = true;
            } else {
                //修改了其他信息
                zyUserInfoVo.setPhone(old.getPhone());
                userInfoService.autoSyncRxkZyUser(zyUserInfoVo);
            }
            // 如果修改了是否是测试机构标记
            if (!old.getTestOrganFlag().equals(record.getTestOrganFlag())) {
                pushFlag = true;
            }
            // 如果修改了是否是Api机构标记
            if (!old.getApiOrganFlag().equals(record.getApiOrganFlag())) {
                pushFlag = true;
            }
            if (saasOrganInfo != null) {
                //如果修改了机构名称
                if (!record.getOrganName().equals(old.getOrganName())) {
                    saasOrganInfo.setBelongsAgency(record.getOrganName());
                    pushFlag = true;
                }
                //如果修改了机构简称，融享客saas管理，机构简称页面没有 为null
                if (null != record.getShortName() && !record.getShortName().equals(saasOrganInfo.getShortName())) {
                    saasOrganInfo.setShortName(record.getShortName());
                    pushFlag = true;
                }
                if (null != record.getUseStatus()) {
                    pushFlag = true;
                }
                if (null != record.getMaxOrderCount()) {
                    saasOrganInfo.setMaxOrderCount(record.getMaxOrderCount());
                    pushFlag = true;
                }
            }
            boolean updateState = userInfoService.updateByPrimaryKeySelective(record);
            if (updateState) {
                UserInfo pushInfo = new UserInfo();
                pushInfo.setId(record.getId());
                pushInfo.setParentId(old.getParentId());
                pushInfo.setUserType(old.getUserType());
                pushInfo.setPhone(record.getPhone());
                pushInfo.setRealName(record.getRealName());
                pushInfo.setTestOrganFlag(record.getTestOrganFlag());
                pushInfo.setApiOrganFlag(record.getApiOrganFlag());
                pushInfo.setOrganName(record.getOrganName());
                pushInfo.setUserLicense(old.getUserLicense());
                pushInfo.setSaleUserName(record.getSaleUserName());
                pushInfo.setCheckDb(record.getCheckDb());
                this.userInfoService.postCheckDbStatusSwitch(old.getCheckDb(), record);
                // 如果修改了销售跟进人
                if (record.getSaleUserId() == null) {
                    // 那么将销售跟进人置为空
                    this.userInfoService.emptySaleUserInfoById(record.getId());
                }
                if (pushFlag && saasOrganInfo != null) {
                    // 如果禁用了账户
                    if (Ints.compare(record.getUseStatus(), 0) == 0) {
                        saasOrganInfo.setCpsSwitch(0);
                        saasOrganInfo.setExtension(0);
                    }
                    saasOrganInfo.setBelongsAgency(record.getOrganName());
                    saasOrganInfoService.saveOrUpdate(saasOrganInfo);
                }
                //如果修改了机构名称
                if (!record.getOrganName().equals(old.getOrganName())) {
                    String lowerUserIds = userInfoService.getLowerUserIdsForSaasIncludeMe(record.getId());
                    userInfoService.updateOrganNameByUserIds(record.getOrganName(), lowerUserIds);
                }
                //如果修改了剩余时间
                boolean updateTimeFlag = record.getUseExpireTime() != null && (old.getUseExpireTime() == null || record.getUseExpireTime().compareTo(old.getUseExpireTime()) != 0);
                if (updateTimeFlag) {

                    //计算系统剩余时间
                    long expireDay = DatetimeUtil.getDayInterval(new Date(), record.getUseExpireTime());
                    if (expireDay >= 3) {
                        //重置系统用户到期短信召回状态
                        userRecallMsgSendService.resetSaasSystemExpireRecallStatus(record.getId(), expireDay);
                    }
                }
                // 如果为SAAS机构用户，更新用户缓存
                if (old.getUserType().compareTo(UserInfoTypeEnum.DENIED.getType()) == 0) {
                    // 如果是saas用户，推送钱包用户信息
                    CompletableFuture.runAsync(() -> this.pushExecutePool.pushOrganInfo2Wallet(pushInfo));
                    //saas2.6.3临时需求 扣费方式配置处理
                    castUserBillingTypeConfig(record);
                    //更改用户历史未支付订单改为已过期
                    CompletableFuture.runAsync(() -> {
                        final String lockKey = RedisPrefixConstants.SAAS_USER_RENEW_RECORD_LOCK + record.getId();
                        String lockValue = null;
                        try {
                            lockValue = this.redissonUtil.lock(lockKey, 15000, 16000);
                            if (null != lockValue) {
                                //更改用户历史未支付订单改为已过期
                                saasUserRenewRecordService.updateNoPaySaasRenewRecordByUserId(record.getId());
                            }
                        } catch (Exception e) {
                            logger.error("更改用户历史未支付的订单改为已过期异常", e);
                        } finally {
                            if (null != lockValue) {
                                this.redissonUtil.unlock(lockKey);
                            }
                        }
                    });
                    //推广总开关关闭 下架所有产品
                    if (record.getAllowProductPromotion() != null && record.getAllowProductPromotion() == 0) {
                        logger.warn("关闭机构产品推广功能，机构id:{},操作人：{}", old.getId());
                        Shop shop = new Shop();
                        shop.setUserId(old.getId());
                        shop.setSwitchStatus(SwitchEnum.CLOSE.getIntCode());
                        shopService.extendSwitchForSaaS(shop);
                    }
                    if (null != record.getVisitOrderSwitch()) {
                        record.setParentId(old.getParentId());
                        this.organUserCacheService.setUserCache(record);
                    }
                    //自动同步组织架构
//                    workWechatUserService.autoSyncUser(record.getId());
                } else if (old.getUserType().compareTo(UserInfoTypeEnum.CLIENT.getType()) == 0) {
                    // 如果是saas本地用户，推送钱包用户信息
                    CompletableFuture.runAsync(() -> this.pushExecutePool.pushAddSaasClientUserInfo(pushInfo));
                }
            }
            if (!StrUtil.isEmpty(record.getRealName()) && !record.getRealName().equals(oldRealName)) {
                Integer userType = old.getUserType();
                //saas用户或saas本地化用户
                boolean saasUserFlag = userType != null
                        && (Ints.compare(userType, UserInfoUserTypeEnum.USER_TYPE_200.getIntCode()) == 0
                        || Ints.compare(userType, UserInfoUserTypeEnum.USER_TYPE_300.getIntCode()) == 0);
                if (saasUserFlag) {
                    appExecutePool.updateUserRealName(userInfoService.getMainAdminUserId(record.getId()), record.getId(), record.getRealName());
                }
            }

            //维护机构菜单
            userInfoService.updateUserOrganMenu(old, record);

            // 融享客展业机构关联更新
            if (null != record.getZyOrganId() && !StringUtil.isEmpty(record.getZyOrganName())) {
                saasZyOrganRelationService.insertOrUpdate(record);
            }
            boolean bool = (old.getUserType().compareTo(UserInfoUserTypeEnum.USER_TYPE_200.getIntCode()) == 0 || old.getUserType().compareTo(UserInfoUserTypeEnum.USER_TYPE_300.getIntCode()) == 0)
                    && old.getParentId() == 0;
            if (bool) {
                // 融享客saas管理，页面没有 为null
                if (null != record.getSaasProductTypeId()) {
                    SaasProductType saasProductType = saasProductTypeService.selectByPrimaryKey(record.getSaasProductTypeId());
                    if (saasProductType != null) {
                        appExecutePool.pushToQbProductType(record.getPhone(), record.getUserType(), saasProductType.getName());
                    }
                }
            }
            //呼叫中心同步机构名称
            yocCallCenterService.syncCallCenterOrganName(record.getOrganName(), record.getId(), false);
            return RespHandler.success(updateState);
        } catch (ApplicationException e) {
            logger.error("修改失败！", e);
            return RespHandler.error(e.getMessage());
        } catch (Exception e) {
            logger.error("修改失败！", e);
            return RespHandler.error("修改失败！");
        }
    }

    /**
     * Title: 修改单个用户销售人员
     * Description:
     * <p>
     * * @param record
     */
    @RequestMapping(value = "/updateOperate", method = RequestMethod.POST)
    public RespHandler updateOperate(@RequestParam(value = "id") Integer id, @RequestParam(value = "updateOperateUserId") Integer updateOperateUserId) {
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(id);
            userInfo.setOperateUserId(updateOperateUserId);
            return RespHandler.success(userInfoService.updateByPrimaryKeySelective(userInfo));
        } catch (Exception e) {
            logger.error("修改单个用户销售人员！", e);
            return RespHandler.error("修改单个用户销售人员！");
        }
    }

    /**
     * Title: 批量修改用户销售人员
     * Description:
     * <p>
     * * @param record
     */
    @RequestMapping(value = "/updateBatchOperate", method = RequestMethod.POST)
    public RespHandler updateBatchOperate(@RequestParam(value = "id", required = false) Integer id,
                                          @RequestParam(value = "status", required = false) Integer status,
                                          @RequestParam(value = "realName", required = false) String realName,
                                          @RequestParam(value = "phone", required = false) String phone,
                                          @RequestParam(value = "beginTime", required = false) String beginTime,
                                          @RequestParam(value = "endTime", required = false) String endTime,
                                          @RequestParam(value = "registChannelType", required = false) Integer registChannelType,
                                          @RequestParam(value = "sourceInfo", required = false) String sourceInfo,
                                          @RequestParam(value = "activateTimeStart", required = false) String activateTimeStart,
                                          @RequestParam(value = "activateTimeEnd", required = false) String activateTimeEnd,
                                          @RequestParam(value = "authSuccessTimeStart", required = false) String authSuccessTimeStart,
                                          @RequestParam(value = "authSuccessTimeEnd", required = false) String authSuccessTimeEnd,
                                          @RequestParam(value = "city", required = false) String city,
                                          @RequestParam(value = "pushStatus", required = false) Integer pushStatus,
                                          @RequestParam(value = "userType", required = false, defaultValue = "0") Integer userType,
                                          @RequestParam(value = "vipType", required = false) Integer vipType,
                                          @RequestParam(value = "shopId", required = false) Integer shopId,
                                          @RequestParam(value = "shopStatus", required = false) Integer shopStatus,
                                          @RequestParam(value = "shopStartTime", required = false) String shopStartTime,
                                          @RequestParam(value = "shopEndTime", required = false) String shopEndTime,
                                          @RequestParam(value = "operateUserId", required = false) Integer operateUserId,
                                          @RequestParam(value = "updateOperateUserId") Integer updateOperateUserId) {
        try {
            Map<String, Object> param = new HashMap<String, Object>(16);
            param.put("id", id);
            param.put("status", status);
            param.put("phone", phone);
            param.put("realName", realName);
            param.put("beginTime", beginTime);
            param.put("endTime", endTime);
            param.put("registChannelType", registChannelType);
            param.put("sourceInfo", sourceInfo);
            param.put("activateTimeStart", activateTimeStart);
            param.put("activateTimeEnd", activateTimeEnd);
            param.put("authSuccessTimeStart", authSuccessTimeStart);
            param.put("authSuccessTimeEnd", authSuccessTimeEnd);
            param.put("city", city);
            param.put("pushStatus", pushStatus);
            param.put("userType", userType);
            param.put("shopId", shopId);
            param.put("shopStatus", shopStatus);
            param.put("shopStartTime", shopStartTime);
            param.put("shopEndTime", shopEndTime);
            param.put("operateUserId", operateUserId);
            List<Integer> idList = userInfoService.selectIdList(param);
            int i = userInfoService.updateBatchOperate(idList, updateOperateUserId);
            return RespHandler.success("共修改" + i + "个运营归属人！");
        } catch (Exception e) {
            logger.error("批量修改用户销售人员！", e);
            return RespHandler.error("批量修改用户销售人员！");
        }
    }

    /**
     * 修改展业用户手机号
     *
     * @param phone
     * @param id
     * @return
     */
    @PostMapping(value = "/updatePhone")
    public RespHandler<Object> updatePhone(@RequestParam("phone") String phone,
                                           @RequestParam("id") Integer id) throws Exception {
        //判断手机是否存在
        UserInfo userInfo = userInfoService.selectByPrimaryKey(id);
        if (userInfo.getPhone().equals(phone)) {
            return RespHandler.success();
        }
        int userType = userInfo.getUserType();
        //判断手机是否存在
        int users = userInfoService.getUserInfoByPhoneAndIdNot(phone, String.valueOf(userInfo.getUserType()), id);
        if (users > 0) {
            return RespHandler.error("该手机号已存在！");
        }

        // 融享客展业用户，还要检查saas融享客用户手机号是否存在
        if (userType == UserInfoUserTypeEnum.USER_TYPE_60.getIntCode()) {
            //判断手机是否存在
            int rxkUsers = userInfoService.getUserInfoByPhoneAndIdNot(phone, UserInfoUserTypeEnum.USER_TYPE_200.getCode(), null);
            if (rxkUsers > 0) {
                return RespHandler.error("该手机号已存在！");
            }
        }
        return RespHandler.success(userInfoService.updatePhone(phone, id, getLoginUserId()));
    }

    /**
     * Title: delete
     * Description:
     * <p>
     * * @param record
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public RespHandler delete(Integer id) {
        try {
            userInfoService.deleteByPrimaryKey(id);
            return RespHandler.success(userInfoService.deleteByPrimaryKey(id));
        } catch (Exception e) {
            logger.error("删除失败！", e);
            return RespHandler.error("删除失败！");
        }
    }

    /**
     * Title: delete
     * Description:
     * <p>
     * * @param record
     */
    @RequestMapping(value = "/selectForSendCpn", method = RequestMethod.GET)
    public RespHandler<Object> selectForSendCpn(UserInfoParameter entity) {
        try {
            List<UserInfoVo> userInfos = userInfoService.selectList(entity);
            String phoneStr = "";
            String userIdStr = "";
            int total = 0;
            if (!CollectionUtils.isEmpty(userInfos)) {
                total = userInfos.size();
                List<Integer> userIdList = userInfos.stream().map(UserInfoVo::getId).collect(Collectors.toList());
                List<String> phoneList = userInfos.stream().map(UserInfoVo::getPhone).collect(Collectors.toList());
                userIdStr = StringUtils.join(userIdList, ",");
                phoneStr = StringUtils.join(phoneList, "\n");
            }
            Map<String, Object> map = new HashMap<>(16);
            map.put("phoneStr", phoneStr);
            map.put("userIdStr", userIdStr);
            map.put("total", total);
            return RespHandler.success(map);
        } catch (Exception e) {
            logger.error("查询异常！", e);
            return RespHandler.error("查询异常！");
        }
    }


    /**
     * 根据用户id获取用户信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/getUserInfoById", method = RequestMethod.GET)
    public RespHandler getUserInfoById(Integer id) {
        try {
            return RespHandler.success(userInfoService.getInfoById(id));
        } catch (Exception e) {
            logger.error("获取数据失败", e);
            return RespHandler.error("获取数据失败");
        }
    }


    /**
     * 删除机构用户信息
     * deleteUser
     */
    @RequestMapping(value = "/deleteUserById", method = RequestMethod.POST)
    public RespHandler deleteUserById(@RequestParam Integer id, @RequestParam String code) {
        try {
            SysUser sysUser = sysUserService.getUserByUserId(getLoginUserId());
            return userInfoService.deleteUserById(id, sysUser.getPhone(), code);
        } catch (Exception e) {
            logger.error("删除失败！", e);
            return RespHandler.error("删除失败！");
        }
    }

    /**
     * 用户列表导出
     *
     * @param entity
     */
    @GetMapping("/exportExcel")
    public void exportExcel(UserInfoParameter entity) {
        try {
            Integer i = 0;
            List<UserInfoVo> userInfos = userInfoService.selectList(entity);
            for (UserInfoVo item : userInfos) {
                UserInfoAuth userInfoAuth = userInfoAuthMapper.getRecordSuccessByUserId(item.getId());
                if (null != userInfoAuth) {
                    item.setBasicInfoAuthTime(userInfoAuth.getBasicInfoAuthTime());
                    item.setCity(userInfoAuth.getCity());
                }
                item.setPhone(PhoneUtil.mobileEncrypt4(item.getPhone()));
                item.setRealName(TuoMinUtil.nameEncrypt(item.getRealName()));
                item.setLoginName(TuoMinUtil.nameEncrypt(item.getLoginName()));
                if ("1".equals(item.getStatus())) {
                    item.setStatus("未全部认证");
                } else {
                    item.setStatus("已全部认证");
                }
                if (i.equals(item.getShopStatus())) {
                    item.setShopStatusStr("关闭");
                } else {
                    item.setShopStatusStr("开启");
                }
                if (i.equals(item.getPushStatus())) {
                    item.setPushStatusStr("关闭");
                } else {
                    item.setPushStatusStr("开启");
                }
                if (i.equals(item.getVipType())) {
                    item.setVipStatusStr("普通用户");
                } else {
                    item.setVipStatusStr("会员用户");
                }
            }
            List<UserInfoExcelDto> userInfoExcelDtos = BeanCopyUtils.copyProperties(userInfos, UserInfoExcelDto.class);
            EasyExcelUtils.exportExcel(getResponse(), userInfoExcelDtos, UserInfoExcelDto.class, "信贷员用户列表");
        } catch (Exception e) {
            logger.error("导出用户列表失败！", e);
        }
    }

    /**
     * 更新获客渠道
     */
    @PostMapping(value = "/updateGuestChannel")
    public RespHandler updateGuestChannel(@RequestParam("id") Integer id, @RequestParam("guestChannel") int guestChannel) {
        try {
            userInfoService.updateGuestChannel(id, guestChannel);
            return RespHandler.success("操作成功");
        } catch (Exception e) {
            logger.error("操作异常！", e);
            return RespHandler.error("系统繁忙，请稍后再试！");
        }
    }

    /**
     * 批量修改获客渠道
     *
     * @param ids          用户id字符串
     * @param phones       手机号字符串
     * @param organName    机构名称
     * @param guestChannel 获客渠道 必传
     */
    @PostMapping(value = "/batchUpdateGuestChannel")
    public RespHandler batchUpdateGuestChannel(@RequestParam(value = "ids", required = false) String ids,
                                               @RequestParam(value = "phones", required = false) String phones,
                                               @RequestParam(value = "organName", required = false) String organName,
                                               @RequestParam(value = "guestChannel") Integer guestChannel,
                                               @RequestParam(value = "userType") Integer userType) {
        try {
            userInfoService.batchUpdateGuestChannel(ids, phones, organName, guestChannel, userType);
            return RespHandler.success("操作成功");
        } catch (Exception e) {
            logger.error("操作异常！", e);
            return RespHandler.error("系统繁忙，请稍后再试！");
        }
    }

    /**
     * 管理员机构列表, 展业权限开关
     *
     * @author czl
     * @date 2021/9/8
     */
    @PostMapping("/saasZyOrganSwitch")
    public RespHandler saasZyOrganSwitch(SaasZyOrganVo vo) {
        if (null == vo.getUserId() || null == vo.getSwitchStatus()) {
            return RespHandler.error("参数错误！");
        }
        return userInfoService.saasZyOrganSwitch(vo);
    }

    @PostMapping("/userAccountTransfer")
    public RespHandler userAccountTransfer(@RequestParam("preUserId") int preUserId,
                                           @RequestParam("afterUserId") int afterUserId,
                                           @RequestParam(value = "coins", required = false) String coins) throws DAOException {
        userInfoService.userAccountTransfer(preUserId, afterUserId, coins);
        return RespHandler.success("转移成功");
    }


    /**
     * 用户后台手工充值
     *
     * @param phones      手机号字符串
     * @param chargeMoney 充值金额
     * @param userType    有信展业0  普惠展业50  融享客展业60
     * @return
     */
    @PostMapping(value = "/manualRecharge")
    public RespHandler manualRecharge(@RequestParam(value = "phones") String phones,
                                      @RequestParam(value = "chargeMoney") BigDecimal chargeMoney,
                                      @RequestParam(value = "userType") String userType) {
        String token = null;
        Integer userId = getLoginUserId();
        final String key = RedisPrefixConstants.BACK_MANUAL_RECHARGE + "userId:" + userId;
        try {
            token = redissonUtil.lock(key, 60000, 11000);
            if (null == token) {
                return RespHandler.error("请稍后！");
            }

            return userInflowService.manualRecharge(phones, userType, chargeMoney, userId);
        } catch (Exception e) {
            logger.error("手工充值异常！userId={}, phones={}", userId, phones, e);
            return RespHandler.error("手工充值异常！");
        } finally {
            if (token != null) {
                redissonUtil.unlock(key);
            }
            remove();
        }
    }

    /**
     * 用户后台手工充值列表
     *
     * @param parameter
     * @return
     */
    @GetMapping(value = "/selectManualRechargeList")
    public RespHandler selectManualRechargeList(ManualRechargeParameter parameter) {
        PageHelper.startPage(getPageNum(), getPageSize());
        List<UserInflowVo> voList = userInflowService.selectManualRechargeList(parameter);
        voList.forEach(e -> {
            e.setPhone(TuoMinUtil.nameEncrypt(e.getPhone()));
            e.setHandler(TuoMinUtil.nameEncrypt(e.getHandler()));
        });
        return RespHandler.success(new PageBean<>(voList));
    }


    /**
     * saas用户后台赠送金币/扣减金币
     *
     * @param userId      saas用户id
     * @param phone       操作人手机号
     * @param code        验证码
     * @param chargeMoney 充值金额
     * @param chargeType  充值类型 1-充值；2-减扣
     * @return
     */
    @PostMapping(value = "/giveVoucherRecharge")
    public RespHandler giveVoucherRecharge(@RequestParam(value = "userId") Integer userId,
                                           @RequestParam(value = "phone") String phone,
                                           @RequestParam(value = "code") String code,
                                           @RequestParam(value = "chargeMoney") BigDecimal chargeMoney,
                                           @RequestParam(value = "chargeType") Integer chargeType) {
        int enable = Integer.parseInt(sysParamService.getSysPramByCode(SysParamConstants.IS_ENABLE_CODE_LOGIN).getValue());
        //是否开启验证码校验 0-否，1-是
        if (enable == CommonEnum.Whether.YES.getIntCode()) {
            String key = RedisPrefixConstants.ADMIN_PHONE_CODE + encDecHandler.encrypt(phone) + ":userType:" + UserInfoUserTypeEnum.USER_TYPE_0.getIntCode();
            if (!redisTemplateUtil.exists(key) || !redisTemplateUtil.get(key).equalsIgnoreCase(code)) {
                redisTemplateUtil.del(key);
                return RespHandler.error("验证码错误！");
            }
            redisTemplateUtil.del(key);
        }
        userInflowService.giveVoucherRecharge(userId, chargeMoney, chargeType, getLoginUserId());
        return RespHandler.success("操作成功");
    }

    /**
     * saas用户后台赠送记录
     *
     * @param beginTime
     * @param endTime
     * @param transType
     * @param handler
     * @return
     */
    @GetMapping(value = "/selectGiveVoucherRechargeList")
    public RespHandler selectGiveVoucherRechargeList(@RequestParam(value = "beginTime", required = false) String beginTime,
                                                     @RequestParam(value = "endTime", required = false) String endTime,
                                                     @RequestParam(value = "transType", required = false) Integer transType,
                                                     @RequestParam(value = "handler", required = false) String handler,
                                                     @RequestParam("userId") Integer userId) {
        return RespHandler.success(new PageBean<>(userTransDetailService.selectGiveVoucherRechargeList(beginTime, endTime, transType, handler, userId)));
    }

    /**
     * saas用户后台赠送记录的查询合计
     *
     * @param beginTime
     * @param endTime
     * @param transType
     * @param handler
     * @return
     */
    @GetMapping(value = "/getTotalGiveVoucherRecharge")
    public RespHandler getTotalGiveVoucherRecharge(@RequestParam(value = "beginTime", required = false) String beginTime,
                                                   @RequestParam(value = "endTime", required = false) String endTime,
                                                   @RequestParam(value = "transType", required = false) Integer transType,
                                                   @RequestParam(value = "handler", required = false) String handler,
                                                   @RequestParam("userId") Integer userId) {
        return RespHandler.success(userTransDetailService.getTotalGiveVoucherRecharge(beginTime, endTime, transType, handler, userId));
    }


    /**
     * 解除机构登录限制
     *
     * @return
     */
    @RequestMapping(value = "/relieveLoginLimit", method = RequestMethod.POST)
    public RespHandler relieveLoginLimit(@RequestParam Integer userId) {

        try {
            loginLimitService.relieveLoginLimit(userId);
            return RespHandler.success();
        } catch (Exception e) {
            logger.error("解除机构登录限制！" + e.getMessage(), e);
            return RespHandler.error("解除机构登录限制异常!", null);
        }

    }

    /**
     * 查询机构关联流量包配置
     *
     * @param organId 机构id
     * @return {@link RespHandler<PageBean<SaasFlowPackageOrganDO>> }
     */
    @PostMapping("/flowPackageConfig")
    public RespHandler<List<SaasFlowPackageOrganDO>> flowPackageConfig(Integer organId) {
        AssertUtil.isTrue(ObjectUtil.isNotNull(organId), "参数错误");
        logger.info("查询机构关联流量包配置，机构id：{}", organId);
        return RespHandler.success(saasFlowPackageOrganService.listByOrgan(organId));
    }

    /**
     * 保存机构流量包配置
     *
     * @param param 参数
     * @return {@link RespHandler<Boolean> }
     */
    @PostMapping("/flowPackageConfig/save")
    public RespHandler<Boolean> saveFlowPackageConfig(@RequestBody SaasOrganClueConfigDTO param) {
        AssertUtil.isTrue(ObjectUtil.isNotNull(param.getOrganId()), "参数错误");
        logger.info("保存机构关联流量包配置，机构id：{}", param.getOrganId());
        return RespHandler.success(saasFlowPackageOrganService.saveFlowPackageConfig(param));
    }

    @PostMapping("/organ/dropdownList")
    public RespHandler<List<UserInfo>> selectOrganDropdownList() {
        return RespHandler.success(userInfoService.selectOrganDropdownList());
    }

}
