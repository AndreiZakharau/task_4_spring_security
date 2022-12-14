package com.epam.esm.controller;


import com.epam.esm.dto.orderDto.CreateOrder;
import com.epam.esm.dto.orderDto.ReadOrder;
import com.epam.esm.dto.userDto.CreateUser;
import com.epam.esm.dto.userDto.ReadUser;
import com.epam.esm.dto.userDto.UserDto;
import com.epam.esm.exception.AuthDateException;
import com.epam.esm.link.linkImpl.AddOrderLink;
import com.epam.esm.link.linkImpl.AddUserLink;
import com.epam.esm.security.filter.IsValidUser;
import com.epam.esm.service.impl.OrderServiceImpl;
import com.epam.esm.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {


    private final UserServiceImpl userService;
    private final OrderServiceImpl orderService;
    private final AddUserLink userLink;
    private final AddOrderLink orderLink;
    private final IsValidUser validUser;

    /**
     * Created new user
     *
     * @param user the CreateUser (user Dto)
     * @return new CreateUser (user Dto)
     */
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUser addUser(@RequestBody CreateUser user) {
        userService.saveEntity(user);
        return user;
    }

    /**
     * @param page the page
     * @param size the size
     * @return readUser (user Dto)
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public CollectionModel<ReadUser> listAllUsers(@RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
                                                  @RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {

        ReadUser readUser = new ReadUser();
        Page<ReadUser> models = userService.getAllEntity(page, size);
        userLink.pageLink(models, readUser);
        return CollectionModel.of(models.stream().peek(userLink::addLinks)
                        .collect(Collectors.toList()),
                readUser.getLinks());
    }

    /**
     * Get user by id
     *
     * @param id the id
     * @return readUser (user Dto)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @ResponseStatus(HttpStatus.OK)
    public ReadUser getUserById(@PathVariable long id) {
        if (validUser.isValidId(id)) {
            Optional<ReadUser> userModel = Optional.ofNullable(userService.findById(id)).get();
            userLink.addLinks(userModel.get());
            return userModel.get();
        } else {
            throw new AuthDateException("message.user.forbidden");
        }
    }
    /**
     * get readUser by name
     *
     * @param name the name
     * @return readUser
     */
    @GetMapping("/{name}/name")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @ResponseStatus(HttpStatus.OK)
    public ReadUser getUserByName(@PathVariable String name) {
        if (validUser.isValidName(name)) {
            ReadUser userModel = userService.getUserByName(name);
            userLink.addLinks(userModel);
            return userModel;
        } else {
            throw new AuthDateException("message.user.forbidden");
        }
    }
    /**
     * update userDto by id
     *
     * @param userDto the user Dto
     * @param id      the id
     * @return the exposed readUser (user Dto)
     */

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUser(@RequestBody UserDto userDto, @PathVariable long id) {

        if (validUser.isValidId(id)) {
            return userService.updateEntity(id, userDto);
        } else {
            throw new AuthDateException("message.user.forbidden");
        }
    }

    /**
     * delete userDto by id
     *
     * @param id the id
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long id) {
        if (validUser.isValidId(id)) {
            userService.deleteEntity(id);
        } else {
            throw new AuthDateException("message.user.forbidden");
        }
    }



    @GetMapping("/orders")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CollectionModel<ReadOrder> getOrders(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
        ReadOrder readOrder = new ReadOrder();
        Page<ReadOrder> models = orderService.getAllEntity(page, size);
        orderLink.pageLink(models, readOrder);
        return CollectionModel.of(models.stream().peek(orderLink::addLinks).collect(Collectors.toList()), readOrder.getLinks());
    }

    /**
     * @param id            the userID
     * @param certificateId the CertificateId
     * @return OrderModel
     */
    @PostMapping("{id}/orders")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrder purchaseCertificate(@PathVariable long id, @RequestParam long certificateId) {
        if (validUser.isValidId(id)) {
            return userService.purchaseCertificate(id, certificateId);
        } else {
            throw new AuthDateException("message.user.forbidden");
        }

    }

    /**
     * Get order by user id
     *
     * @param id the user id
     * @return readOrder (order Dto)
     */
    @GetMapping("{id}/orders")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @ResponseStatus(HttpStatus.OK)
    public CollectionModel<ReadOrder> getOrderByUserId(@PathVariable long id,
                                                       @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                       @RequestParam(value = "size", defaultValue = "10", required = false) int size) {

        if (validUser.isValidId(id)) {
            ReadOrder readOrder = new ReadOrder();
            Page<ReadOrder> orders = (orderService.getOrdersByUserId(id, page, size));
            orderLink.pageLink(orders, readOrder);
            return CollectionModel.of(orders.stream().peek(orderLink::addLinks).collect(Collectors.toList()));
        } else {
            throw new AuthDateException("message.user.forbidden");
        }
    }

    /**
     * delete orderDto by id
     *
     * @param id the id
     */
    @DeleteMapping("{}/orders/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable long id) {
        orderService.deleteEntity(id);
    }


}
