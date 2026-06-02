import type { IngestSource } from "../source";
import type { CarOffer } from "../../lib/types";

/** Always-on sample source so the backend has data with zero configuration. */
export const mockSource: IngestSource = {
  sourceId: "mock",
  displayName: "Sample data",
  isEnabled: () => true,
  async fetch(): Promise<CarOffer[]> {
    const now = Date.now();
    const h = (n: number) => now - n * 3_600_000;
    return [
      {
        id: "mock:1", sourceId: "mock", title: "BMW 320d 2018 Touring",
        make: "BMW", model: "320d", year: 2018, mileageKm: 142_000,
        price: { amount: 78_900, currency: "PLN" },
        fuelType: "DIESEL", transmission: "AUTOMATIC", powerHp: 190,
        location: "Krakow, PL", region: "POLAND",
        thumbnailUrl: null, imageUrls: [],
        listingUrl: "https://example.com/listing/1", postedAtEpochMs: h(3),
      },
      {
        id: "mock:2", sourceId: "mock", title: "Audi A4 2.0 TFSI 2019",
        make: "Audi", model: "A4", year: 2019, mileageKm: 98_000,
        price: { amount: 19_500, currency: "EUR" },
        fuelType: "PETROL", transmission: "AUTOMATIC", powerHp: 190,
        location: "Berlin, DE", region: "EUROPE",
        thumbnailUrl: null, imageUrls: [],
        listingUrl: "https://example.com/listing/2", postedAtEpochMs: h(20),
      },
      {
        id: "mock:3", sourceId: "mock", title: "Ford Mustang GT 5.0 2020",
        make: "Ford", model: "Mustang", year: 2020, mileageKm: 35_000,
        price: { amount: 18_000, currency: "USD" },
        fuelType: "PETROL", transmission: "AUTOMATIC", powerHp: 460,
        location: "Newark, NJ, USA", region: "USA",
        thumbnailUrl: null, imageUrls: [],
        listingUrl: "https://example.com/listing/3", postedAtEpochMs: h(50),
      },
    ];
  },
};
