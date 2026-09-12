package com.example.shared.contracts

/**
 * Web Admin API Contracts & React/TypeScript Interface Definitions for BookZzz
 * This file acts as the formal contract between:
 * 1. Mobile Client (Android Jetpack Compose)
 * 2. Web Admin Portal (React / TypeScript / Tailwind)
 * 3. Shared Database / Backend Engine
 */

object WebAdminContracts {

    /**
     * TypeScript interfaces ready to be copied or exported to the React Web Admin codebase
     */
    const val TYPESCRIPT_DEFINITIONS = """
/**
 * BOOKZZZ - Shared TypeScript Definitions for Web Admin Portal (React / Next.js)
 * Generated from KMP Shared Module
 */

export type ServiceType = 'HOTEL_ROOMS' | 'FURNISHED_APARTMENTS' | 'REAL_ESTATE_RENTALS' | 'VEHICLES';

export type BookingStatus = 
  | 'PENDING_PAYMENT' 
  | 'CONFIRMED' 
  | 'CHECKED_IN' 
  | 'COMPLETED' 
  | 'RESCHEDULED' 
  | 'CANCELLED' 
  | 'REJECTED';

export interface GeoLocation {
  latitude: number;
  longitude: number;
  address: string;
  city: string;
  commune?: string;
}

// 1. HOTEL & ROOM CRUD
export interface HotelAdminDto {
  id: string;
  name: string;
  description: string;
  city: string;
  address: string;
  basePricePerNight: number;
  rating: number;
  phonePaymentNumber: string;
  operatorName: 'M-Pesa' | 'Airtel Money' | 'Orange Money';
  coordinates?: GeoLocation;
  amenities: string[];
  images: string[];
  isSuspended: boolean;
  totalRoomsCount?: number;
}

export interface RoomAdminDto {
  id: string;
  hotelId: string;
  roomNumber: string;
  type: string; // 'Chambre Standard' | 'Suite Deluxe' | 'Suite Présidentielle'
  pricePerNight: number;
  capacityAdults: number;
  capacityChildren: number;
  amenities: string[];
  isAvailableByDefault: boolean;
}

// 2. FURNISHED APARTMENTS (Court/Moyen séjour)
export interface ApartmentAdminDto {
  id: string;
  title: string;
  description: string;
  city: string;
  address: string;
  pricePerNight: number;
  pricePerMonth?: number;
  numberOfRooms: number;
  numberOfBathrooms: number;
  maxGuests: number;
  isWaterAndElectricity247: boolean;
  images: string[];
  ownerPhone: string;
  isAvailable: boolean;
}

// 3. REAL ESTATE LISTINGS (Location Longue Durée)
export interface RealEstateListingDto {
  id: string;
  title: string;
  propertyType: 'Villa' | 'Appartement' | 'Maison Basse' | 'Studio' | 'Commercial';
  city: string;
  commune: string;
  monthlyRentUSD: number;
  cautionMonths: number;
  advanceMonths: number;
  description: string;
  features: string[];
  agencyName: string;
  contactPhone: string;
  availableFromDate: string;
  isPublished: boolean;
}

// 4. VEHICLES & SHUTTLES
export interface VehicleAdminDto {
  id: string;
  modelName: string;
  category: 'SUV 4x4' | 'Berline VIP' | 'Minibus Navette';
  seats: number;
  pricePerDayUSD: number;
  priceAirportTransferUSD: number;
  hasChauffeurIncluded: boolean;
  driverName?: string;
  driverPhone?: string;
  isAvailable: boolean;
}

// 5. BOOKING MANAGEMENT & DATE RESCHEDULING
export interface DateReschedulePayload {
  bookingId: string;
  newArrivalDate: string;   // 'YYYY-MM-DD'
  newDepartureDate: string; // 'YYYY-MM-DD'
  rescheduleReason: string;
  retainTransactionId: boolean; // Keep the original payment valid
}

export interface BookingAdminDto {
  id: string;
  serviceCategory: ServiceType;
  targetItemId: string;
  targetItemName: string;
  roomId?: string;
  roomType: string;
  userId: string;
  userName: string;
  userPhone: string;
  arrivalDate: string;
  departureDate: string;
  numNights: number;
  pricePerNight: number;
  totalAmount: number;
  currency: 'USD' | 'CDF';
  status: BookingStatus;
  transactionId: string;
  operatorSelected: string;
  paymentDate: string;
  taxiRequested: boolean;
  dateChangeHistory?: Array<{
    previousArrival: string;
    previousDeparture: string;
    newArrival: string;
    newDeparture: string;
    shiftedAt: string;
    reason: string;
  }>;
}
"""
}
