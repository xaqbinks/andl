-- Balatro Seeded Unlocks Mod
-- This script allows unlocks even on seeded runs.

print("Seeded Unlocks Mod: Patching game functions...")

-- We assume the game has a function to check if the run is seeded.
-- Based on standard game development practices, it would be a method on the Game object.
-- We will store the original function, just in case.
local original_is_seeded = Game.is_seeded

-- Replace the function with our own version that always returns false.
function Game:is_seeded()
  print("Seeded Unlocks Mod: Game:is_seeded() called, returning false.")
  return false
end

print("Seeded Unlocks Mod: Patch applied successfully.")
