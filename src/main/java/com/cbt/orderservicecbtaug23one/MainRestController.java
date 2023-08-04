package com.cbt.orderservicecbtaug23one;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1")
public class MainRestController
{

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderstatusRepository orderstatusRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @PostMapping("save/order")
    public ResponseEntity<Order> saveOrder(@RequestBody Order order)
    {
        order.setOrderid(String.valueOf((int)(Math.random()*100000)));
        orderRepository.save(order);
        Orderstatus orderstatus = new Orderstatus();
        orderstatus.setId(String.valueOf((int)(Math.random()*100000)));
        orderstatus.setOrderid(order.getOrderid());
        orderstatus.setStatus("OPEN");
        orderstatusRepository.save(orderstatus);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PostMapping("save/order/status")
    public ResponseEntity<Mono<Payment>> saveOrderStatus(@RequestBody Orderstatus orderstatus)
    {
        orderstatus.setId(String.valueOf((int)(Math.random()*100000)));
        orderstatusRepository.save(orderstatus); // THIS SHOULD BE AN UPDATE OPERATION

        Mono<Payment> response = null;
        if(orderstatus.getStatus().equals("ACCEPTED"))
        {
            // send a request to create payment to payment service
            response =  webClientBuilder.build().post().uri("http://localhost:8072/payment-service/api/v1/save/payment").
                    body(Mono.just(orderstatus),Orderstatus.class).retrieve().bodyToMono(Payment.class);
        }

        //Payment payment = new Payment();
        //payment.setId(String.valueOf((int)(Math.random()*100000)));
        //payment.setOrderid(orderstatus.getOrderid());
        //payment.setOfferid(orderRepository.findById(orderstatus.getOrderid()).get().getOfferid());
        //payment.setStatus("DUE");
        //paymentRepository.save(payment);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
