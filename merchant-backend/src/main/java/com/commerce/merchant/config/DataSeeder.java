package com.commerce.merchant.config;

import com.commerce.merchant.model.Product;
import com.commerce.merchant.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return;

        List<Product> products = List.of(
            Product.builder().name("Sony WH-1000XM5").description("Industry-leading noise canceling headphones with 30-hour battery life and crystal clear hands-free calling.").price(new BigDecimal("348.00")).category("Electronics").imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400").stock(50).rating(4.8).brand("Sony").tags("headphones,wireless,noise-canceling,audio").build(),
            Product.builder().name("Apple AirPods Pro").description("Active Noise Cancellation for immersive sound. Transparency mode for hearing the world around you.").price(new BigDecimal("249.00")).category("Electronics").imageUrl("https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?w=400").stock(100).rating(4.7).brand("Apple").tags("earbuds,wireless,apple,airpods").build(),
            Product.builder().name("Samsung 4K QLED TV 65\"").description("Quantum HDR, Alexa Built-in, Ultra viewing angle with Quantum Dot technology.").price(new BigDecimal("1299.00")).category("Electronics").imageUrl("https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=400").stock(20).rating(4.6).brand("Samsung").tags("tv,4k,qled,samsung,smart-tv").build(),
            Product.builder().name("iPad Pro 12.9\"").description("The ultimate iPad experience with M2 chip, Liquid Retina XDR display, and all-day battery life.").price(new BigDecimal("1099.00")).category("Electronics").imageUrl("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400").stock(35).rating(4.9).brand("Apple").tags("ipad,tablet,apple,m2").build(),
            Product.builder().name("MacBook Air M3").description("Supercharged by M3 chip, 18-hour battery life, and a stunning Liquid Retina display.").price(new BigDecimal("1299.00")).category("Electronics").imageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400").stock(25).rating(4.9).brand("Apple").tags("laptop,macbook,apple,m3").build(),

            Product.builder().name("Nike Air Max 270").description("The Nike Air Max 270 delivers visible cushioning under every step with a large window of Max Air.").price(new BigDecimal("150.00")).category("Footwear").imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400").stock(200).rating(4.5).brand("Nike").tags("sneakers,nike,running,airmax").build(),
            Product.builder().name("Adidas Ultraboost 23").description("Experience incredible energy return and a sock-like fit with Primeknit upper and Boost midsole.").price(new BigDecimal("190.00")).category("Footwear").imageUrl("https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=400").stock(150).rating(4.6).brand("Adidas").tags("running,adidas,ultraboost,athletic").build(),
            Product.builder().name("Levi's 501 Original Jeans").description("The original blue jean since 1873. Straight fit, button fly, sits at waist.").price(new BigDecimal("69.50")).category("Clothing").imageUrl("https://images.unsplash.com/photo-1542272604-787c3835535d?w=400").stock(300).rating(4.4).brand("Levi's").tags("jeans,denim,levis,casual").build(),
            Product.builder().name("Patagonia Down Jacket").description("Warm, lightweight, and packable. Made with recycled materials for environmental responsibility.").price(new BigDecimal("279.00")).category("Clothing").imageUrl("https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=400").stock(75).rating(4.7).brand("Patagonia").tags("jacket,outdoor,winter,patagonia").build(),

            Product.builder().name("Instant Pot Duo 7-in-1").description("7-in-1 multi-use programmable pressure cooker, slow cooker, rice cooker, steamer, saute pan.").price(new BigDecimal("99.95")).category("Kitchen").imageUrl("https://images.unsplash.com/photo-1585515320310-259814833e62?w=400").stock(80).rating(4.7).brand("Instant Pot").tags("pressure-cooker,kitchen,appliance,cooking").build(),
            Product.builder().name("KitchenAid Stand Mixer").description("Tilt-Head Stand Mixer with 10 speeds and 5-quart stainless steel bowl. Makes up to 9 dozen cookies.").price(new BigDecimal("449.99")).category("Kitchen").imageUrl("https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400").stock(40).rating(4.8).brand("KitchenAid").tags("mixer,baking,kitchen,kitchenaid").build(),
            Product.builder().name("Dyson V15 Detect").description("Our most powerful cord-free vacuum. Laser dust detection reveals invisible dust. Powerful suction throughout.").price(new BigDecimal("749.99")).category("Home").imageUrl("https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400").stock(30).rating(4.8).brand("Dyson").tags("vacuum,dyson,cordless,cleaning").build(),

            Product.builder().name("Yoga Mat Premium").description("Professional 6mm thick non-slip yoga mat with alignment lines. Eco-friendly TPE material.").price(new BigDecimal("68.00")).category("Sports").imageUrl("https://images.unsplash.com/photo-1601925228984-bd17c93a8576?w=400").stock(120).rating(4.5).brand("Manduka").tags("yoga,fitness,exercise,mat").build(),
            Product.builder().name("Theragun Pro").description("Professional-grade percussive therapy device. 16mm amplitude, whisper quiet, 5 speeds.").price(new BigDecimal("599.00")).category("Sports").imageUrl("https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=400").stock(45).rating(4.6).brand("Therabody").tags("massage,recovery,fitness,theragun").build(),
            Product.builder().name("The Psychology of Money").description("Timeless lessons on wealth, greed, and happiness from Morgan Housel. A must-read personal finance book.").price(new BigDecimal("16.99")).category("Books").imageUrl("https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400").stock(500).rating(4.8).brand("Harriman House").tags("finance,money,psychology,books").build(),
            Product.builder().name("Atomic Habits").description("An Easy & Proven Way to Build Good Habits & Break Bad Ones by James Clear.").price(new BigDecimal("14.99")).category("Books").imageUrl("https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=400").stock(400).rating(4.9).brand("Avery").tags("habits,self-help,productivity,books").build()
        );

        productRepository.saveAll(products);
        log.info("Seeded {} products", products.size());
    }
}
