import { describe, it, expect } from "vitest";
import {
  normalizeEmail,
  isValidEmail,
  validatePassword,
  validatePasswordConfirmation,
  cleanRut,
  formatRut,
  isValidRut,
  isValidChilePhone,
  isValidInternalOrHttpUrl,
  isValidImageUrl,
  isValidYouTubeUrl,
  isPositiveNumber,
  isPositiveInteger,
  validateText,
  validateDateRange,
  validateImageUrlList,
} from "../lib/validators";

describe("normalizeEmail", () => {
  it("trims and lowercases", () => {
    expect(normalizeEmail("  User@Example.COM  ")).toBe("user@example.com");
  });
});

describe("isValidEmail", () => {
  it("accepts a well-formed email", () => {
    expect(isValidEmail("user@example.com")).toBe(true);
  });
  it("rejects missing @", () => {
    expect(isValidEmail("userexample.com")).toBe(false);
  });
  it("rejects empty input", () => {
    expect(isValidEmail("")).toBe(false);
  });
  it("rejects emails longer than 120 chars", () => {
    const local = "a".repeat(115);
    expect(isValidEmail(`${local}@x.com`)).toBe(false);
  });
});

describe("validatePassword", () => {
  it("accepts a password with upper, lower and number, 8+ chars", () => {
    expect(validatePassword("Abcd1234")).toBeNull();
  });
  it("rejects empty password", () => {
    expect(validatePassword("")).not.toBeNull();
  });
  it("rejects leading/trailing spaces", () => {
    expect(validatePassword(" Abcd1234")).not.toBeNull();
    expect(validatePassword("Abcd1234 ")).not.toBeNull();
  });
  it("rejects passwords shorter than 8 characters", () => {
    expect(validatePassword("Ab1234")).not.toBeNull();
  });
  it("rejects passwords longer than 72 characters", () => {
    expect(validatePassword(`Ab1${"a".repeat(70)}`)).not.toBeNull();
  });
  it("rejects passwords without an uppercase letter", () => {
    expect(validatePassword("abcd1234")).not.toBeNull();
  });
  it("rejects passwords without a lowercase letter", () => {
    expect(validatePassword("ABCD1234")).not.toBeNull();
  });
  it("rejects passwords without a number", () => {
    expect(validatePassword("Abcdefgh")).not.toBeNull();
  });
});

describe("validatePasswordConfirmation", () => {
  it("accepts matching passwords", () => {
    expect(validatePasswordConfirmation("Abcd1234", "Abcd1234")).toBeNull();
  });
  it("rejects non-matching passwords", () => {
    expect(validatePasswordConfirmation("Abcd1234", "Abcd1235")).not.toBeNull();
  });
});

describe("cleanRut / formatRut", () => {
  it("cleanRut strips dots and dash", () => {
    expect(cleanRut("12.345.678-5")).toBe("123456785");
  });
  it("cleanRut uppercases k", () => {
    expect(cleanRut("1.000.005-k")).toBe("1000005K");
  });
  it("formatRut adds dots and dash", () => {
    expect(formatRut("123456785")).toBe("12.345.678-5");
  });
});

describe("isValidRut", () => {
  it("accepts a valid RUT with dots and dash", () => {
    expect(isValidRut("12.345.678-5")).toBe(true);
  });
  it("accepts a valid RUT without formatting", () => {
    expect(isValidRut("123456785")).toBe(true);
  });
  it("accepts a valid RUT with K check digit", () => {
    expect(isValidRut("1.000.005-K")).toBe(true);
    expect(isValidRut("7.654.321-6")).toBe(true);
  });
  it("rejects a RUT with wrong check digit", () => {
    expect(isValidRut("12.345.678-9")).toBe(false);
    expect(isValidRut("11.111.111-2")).toBe(false);
  });
  it("rejects empty or too-short input", () => {
    expect(isValidRut("")).toBe(false);
    expect(isValidRut("1")).toBe(false);
    expect(isValidRut(undefined)).toBe(false);
  });
  it("rejects non-numeric body", () => {
    expect(isValidRut("abc-5")).toBe(false);
  });
});

describe("isValidChilePhone", () => {
  it("accepts a bare mobile number", () => {
    expect(isValidChilePhone("912345678")).toBe(true);
  });
  it("accepts with +56 prefix", () => {
    expect(isValidChilePhone("+56912345678")).toBe(true);
  });
  it("accepts with 56 prefix (no plus)", () => {
    expect(isValidChilePhone("56912345678")).toBe(true);
  });
  it("accepts with spaces", () => {
    expect(isValidChilePhone("+56 9 1234 5678")).toBe(true);
  });
  it("rejects numbers not starting with 9", () => {
    expect(isValidChilePhone("812345678")).toBe(false);
  });
  it("rejects too few digits", () => {
    expect(isValidChilePhone("912345")).toBe(false);
  });
});

describe("isValidInternalOrHttpUrl", () => {
  it("accepts http and https", () => {
    expect(isValidInternalOrHttpUrl("http://example.com")).toBe(true);
    expect(isValidInternalOrHttpUrl("https://example.com/path")).toBe(true);
  });
  it("rejects javascript: URLs", () => {
    expect(isValidInternalOrHttpUrl("javascript:alert(1)")).toBe(false);
  });
  it("rejects data: URLs", () => {
    expect(isValidInternalOrHttpUrl("data:text/html,<script>alert(1)</script>")).toBe(false);
  });
  it("rejects empty or malformed input", () => {
    expect(isValidInternalOrHttpUrl("")).toBe(false);
    expect(isValidInternalOrHttpUrl("not a url")).toBe(false);
  });
});

describe("isValidImageUrl", () => {
  it("accepts jpg/jpeg/png/webp", () => {
    expect(isValidImageUrl("https://example.com/photo.jpg")).toBe(true);
    expect(isValidImageUrl("https://example.com/photo.jpeg")).toBe(true);
    expect(isValidImageUrl("https://example.com/photo.png")).toBe(true);
    expect(isValidImageUrl("https://example.com/photo.webp")).toBe(true);
  });
  it("accepts with query string after extension", () => {
    expect(isValidImageUrl("https://example.com/photo.png?w=200")).toBe(true);
  });
  it("rejects svg", () => {
    expect(isValidImageUrl("https://example.com/photo.svg")).toBe(false);
  });
  it("rejects non-image URLs", () => {
    expect(isValidImageUrl("https://example.com/document.pdf")).toBe(false);
  });
});

describe("isValidYouTubeUrl", () => {
  it("accepts youtube.com/watch?v=", () => {
    expect(isValidYouTubeUrl("https://www.youtube.com/watch?v=abc123")).toBe(true);
  });
  it("accepts youtu.be/", () => {
    expect(isValidYouTubeUrl("https://youtu.be/abc123")).toBe(true);
  });
  it("accepts youtube.com/embed/", () => {
    expect(isValidYouTubeUrl("https://www.youtube.com/embed/abc123")).toBe(true);
  });
  it("accepts youtube.com/shorts/", () => {
    expect(isValidYouTubeUrl("https://www.youtube.com/shorts/abc123")).toBe(true);
  });
  it("rejects other domains", () => {
    expect(isValidYouTubeUrl("https://vimeo.com/12345")).toBe(false);
  });
});

describe("isPositiveNumber", () => {
  it("accepts positive numbers", () => {
    expect(isPositiveNumber(5)).toBe(true);
    expect(isPositiveNumber("5")).toBe(true);
  });
  it("rejects zero, negative or non-numeric", () => {
    expect(isPositiveNumber(0)).toBe(false);
    expect(isPositiveNumber(-5)).toBe(false);
    expect(isPositiveNumber("abc")).toBe(false);
  });
});

describe("isPositiveInteger", () => {
  it("accepts positive integers", () => {
    expect(isPositiveInteger(5)).toBe(true);
  });
  it("rejects decimals, zero, negative", () => {
    expect(isPositiveInteger(5.5)).toBe(false);
    expect(isPositiveInteger(0)).toBe(false);
    expect(isPositiveInteger(-5)).toBe(false);
  });
});

describe("validateText", () => {
  it("returns null when optional and empty", () => {
    expect(validateText("", { required: false })).toBeNull();
  });
  it("returns an error when required and empty", () => {
    expect(validateText("   ", { required: true, label: "El nombre" })).not.toBeNull();
  });
  it("enforces min length", () => {
    expect(validateText("ab", { min: 3 })).not.toBeNull();
  });
  it("enforces max length", () => {
    expect(validateText("abcdef", { max: 3 })).not.toBeNull();
  });
  it("accepts text within bounds", () => {
    expect(validateText("hello", { required: true, min: 2, max: 10 })).toBeNull();
  });
});

describe("validateDateRange", () => {
  it("requires start and end dates by default", () => {
    expect(validateDateRange("", "")).not.toBeNull();
  });
  it("rejects end date before start date", () => {
    expect(validateDateRange("2026-05-01", "2026-04-01")).not.toBeNull();
  });
  it("rejects equal dates by default (strict campaign rule)", () => {
    expect(validateDateRange("2026-05-01", "2026-05-01")).not.toBeNull();
  });
  it("accepts equal dates when allowEqual is true", () => {
    expect(validateDateRange("2026-05-01", "2026-05-01", { allowEqual: true })).toBeNull();
  });
  it("accepts a valid ascending range", () => {
    expect(validateDateRange("2026-05-01", "2026-06-01")).toBeNull();
  });
});

describe("validateImageUrlList", () => {
  it("requires at least the minimum number of photos", () => {
    expect(validateImageUrlList([], 5)).not.toBeNull();
  });
  it("rejects more than maxItems photos", () => {
    const urls = Array.from({ length: 6 }, (_, i) => `https://example.com/${i}.jpg`);
    expect(validateImageUrlList(urls, 5)).not.toBeNull();
  });
  it("rejects invalid image URLs in the list", () => {
    expect(validateImageUrlList(["https://example.com/a.pdf"], 5)).not.toBeNull();
  });
  it("accepts a valid list within bounds", () => {
    expect(validateImageUrlList(["https://example.com/a.jpg", "https://example.com/b.png"], 5)).toBeNull();
  });
});

