package com.petshop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petshop.Api.ApiResult;
import com.petshop.common.PageResult;
import com.petshop.entity.Pet;
import com.petshop.exception.BusinessException;
import com.petshop.service.PetService;
import com.petshop.utils.FileStorageUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;
    private final FileStorageUtil fileStorageUtil;

    // 构造函数注入
    public PetController(PetService petService, FileStorageUtil fileStorageUtil) {
        this.petService = petService;
        this.fileStorageUtil = fileStorageUtil;
    }
//首页展示
    @GetMapping()
    public ResponseEntity<PageResult<Pet>> getPets(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Pet> pageData = petService.getPets(category, name, page, size);
        return ResponseEntity.ok(PageResult.from(pageData));
    }

    // 卖家私有接口
    @GetMapping("/my")
    public ResponseEntity<PageResult<Pet>> getMyPets(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute("userId") Integer userId
    ) {
        // ✅ 注意参数顺序与接口定义一致
        Page<Pet> pageData = petService.getPetsBySeller(userId, category, name, page, size);
        return ResponseEntity.ok(PageResult.from(pageData));
    }



    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Integer id) {
        Pet pet = petService.getById(id);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pet);
    }
//上架宠物
@PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
public ApiResult<Integer> createPet(
        @RequestPart("pet") @Valid Pet pet,
        @RequestPart(value = "image", required = false) MultipartFile imageFile,
        HttpServletRequest request) { // 注入请求对象

    try {
        // 🔥 关键修改：从请求属性中获取用户ID
        Integer sellerId = (Integer) request.getAttribute("userId");
        if (sellerId == null) {
            throw new BusinessException("用户未登录", 401);
        }
        pet.setSellerId(sellerId); // 确保 Pet 实体有 sellerId 字段

        // 处理图片上传
        if (imageFile != null && !imageFile.isEmpty()) {
            String storedPath = fileStorageUtil.storeFile(imageFile);
            pet.setCoverImage(storedPath);
        }

        pet.setCreateTime(LocalDateTime.now());
        petService.save(pet);

        return ApiResult.success(pet.getId());
    } catch (IOException e) {
        throw new BusinessException("文件上传失败: " + e.getMessage(), 500);
    }
}

//编辑
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<Pet> updatePet(
            @PathVariable Integer id,
            @RequestPart("pet") @Valid Pet pet,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        try {
            Pet updatedPet = petService.updatePet(id, pet, imageFile); // 接收返回的 Pet 对象
            return ApiResult.success(updatedPet); // 返回完整数据
        } catch (BusinessException e) {
            return ApiResult.fail(e.getMessage(), e.getStatusCode());
        } catch (IOException e) {
            return ApiResult.fail("文件处理失败", 500);
        }
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResult<Void>> batchDeletePets(
            @RequestParam("ids") List<Integer> ids) {
        try {
            petService.batchDeleteWithValidation(ids);
            return ResponseEntity.ok(ApiResult.success("删除成功"));
        } catch (BusinessException e) {
            // 将int状态码转换为HttpStatus枚举
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResult.fail(e.getMessage(), e.getStatusCode()));
        }
    }

    //删除
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<Void>> deletePet(@PathVariable Integer id) {
        try {
            petService.deletePetWithValidation(id);
            return ResponseEntity.ok(ApiResult.success("删除成功"));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResult.fail(e.getMessage(), e.getStatusCode()));
        }
    }
}
/*
请求体
{
  "name": "Buddy",
  "category": "DOG",  // 不能为空
  "price": 100.50,    // 必须大于0
  "sellerId": 123,    // 不能为空（整数）
  "gender": 0,        // 0或1
  "age": 3            // 正整数
}*/
