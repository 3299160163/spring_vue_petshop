package com.petshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petshop.entity.Pet;
import com.petshop.exception.BusinessException;
import com.petshop.mapper.OrderMapper;
import com.petshop.mapper.PetMapper;
import com.petshop.service.PetService;
import com.petshop.utils.FileStorageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;



@Slf4j
@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private FileStorageUtil fileStorageUtil;


    //首页展示
//    @Override
//    @Cacheable(value = "petList",
//            key = "#category + ':' + #name + ':' + #page + ':' + #size",
//            unless = "#result == null || #result.records.isEmpty()")
//    public Page<Pet> getPets(String category, String name, int page, int size) {
//        Page<Pet> pageParam = new Page<>(page, size);
//        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(StringUtils.hasText(category), Pet::getCategory, category)
//                .like(StringUtils.hasText(name), Pet::getName, name)
//                .orderByDesc(Pet::getCreateTime);
//        return petMapper.selectPage(pageParam, wrapper);
//    }
    @Override
    @Cacheable(value = "petList",
            key = "(#category != null ? #category : '') + ':' + (#name != null ? #name : '') + ':' + #page + ':' + #size",
            unless = "#result == null || #result.total == 0")
    public Page<Pet> getPets(String category, String name, int page, int size) {
        Page<Pet> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), Pet::getCategory, category)
                .like(StringUtils.hasText(name), Pet::getName, name)
                .orderByDesc(Pet::getCreateTime);
        return petMapper.selectPage(pageParam, wrapper);
    }

    //个人宠物展示
    @Override
    public Page<Pet> getPetsBySeller(Integer sellerId, String category, String name, int page, int size) {
        Page<Pet> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getSellerId, sellerId)
                .eq(StringUtils.hasText(category), Pet::getCategory, category)
                .like(StringUtils.hasText(name), Pet::getName, name);
        return petMapper.selectPage(pageParam, wrapper);
    }

    //编辑
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pet updatePet(Integer id, Pet pet, MultipartFile imageFile) throws IOException {
        // 1. 检查宠物是否存在
        Pet existingPet = petMapper.selectById(id);
        if (existingPet == null) {
            throw new BusinessException("宠物不存在", 404);
        }
        // 2. 处理图片更新
        if (imageFile != null && !imageFile.isEmpty()) {
            // 删除旧图片（仅文件名部分）
            if (existingPet.getCoverImage() != null) {
                fileStorageUtil.deleteFile(existingPet.getCoverImage());
            }
            // 上传新图片并设置完整 URL
            String newImagePath = fileStorageUtil.storeFile(imageFile);
            pet.setCoverImage(newImagePath);
        }
        // 3. 保留不可修改字段
        pet.setId(id);
        pet.setCreateTime(existingPet.getCreateTime());
        // 4. 执行更新
        int rows = petMapper.updateById(pet);
        if (rows == 0) {
            throw new BusinessException("更新失败", 500);
        }
        // 5. 返回更新后的完整对象
        return petMapper.selectById(id);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePetWithValidation(Integer id) throws BusinessException {
        // 1. 验证宠物存在性
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException("宠物不存在", 404);
        }
        try {
            // 2. 先删除关联订单
            int orderRows = orderMapper.deleteByPetId(id);
            log.info("已删除{}条关联订单", orderRows);
            // 3. 删除图片文件
            if (StringUtils.hasText(pet.getCoverImage())) {
                fileStorageUtil.deleteFile(pet.getCoverImage());
            }
            // 4. 删除宠物记录
            if (petMapper.deleteById(id) == 0) {
                throw new BusinessException("删除数据库记录失败", 500);
            }
        } catch (IOException e) {
            throw new BusinessException("文件删除失败: " + e.getMessage(), 500);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDeleteWithValidation(List<Integer> ids) {
        // 校验1：空列表检测
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("ID列表不能为空", HttpStatus.BAD_REQUEST.value()); // 改为.value()
        }
        // 校验2：非法ID过滤
        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException("包含非法ID", HttpStatus.BAD_REQUEST.value()); // 改为.value()
        }
        // 验证存在性
        List<Pet> existingPets = petMapper.selectBatchIds(ids);
        if (existingPets.size() != ids.size()) {
            List<Integer> existingIds = existingPets.stream()
                    .map(Pet::getId)
                    .toList();
            List<Integer> notExistIds = ids.stream()
                    .filter(id -> !existingIds.contains(id))
                    .toList();
            throw new BusinessException("以下ID不存在: " + notExistIds, HttpStatus.NOT_FOUND.value()); // 改为.value()
        }
        // 执行批量删除
        int deleteCount = petMapper.deleteBatchIds(ids);

        // 删除结果验证
        if (deleteCount != ids.size()) {
            throw new BusinessException("部分删除失败，请重试", HttpStatus.INTERNAL_SERVER_ERROR.value()); // 改为.value()
        }
    }
}

